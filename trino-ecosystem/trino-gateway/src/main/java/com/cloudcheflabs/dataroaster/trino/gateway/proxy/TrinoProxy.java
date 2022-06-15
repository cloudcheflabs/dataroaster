package com.cloudcheflabs.dataroaster.trino.gateway.proxy;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;


@Component
public class TrinoProxy implements InitializingBean, DisposableBean {

    private static Logger LOG = LoggerFactory.getLogger(TrinoProxy.class);

    @Autowired
    private Environment env;

    private int port;

    private boolean tlsEnabled;

    private String keystorePath;

    private String keystorePass;

    private String trustStorePath;

    private String trustStorePass;

    @Autowired
    private TrinoProxyServlet trinoProxyServlet;

    private Server server;

    public TrinoProxy() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        port = Integer.valueOf(env.getProperty("trino.proxy.port"));
        tlsEnabled = Boolean.valueOf(env.getProperty("trino.proxy.tls.enabled"));
        keystorePath = env.getProperty("trino.proxy.tls.keystorePath");
        keystorePass = env.getProperty("trino.proxy.tls.keystorePass");
        trustStorePath = env.getProperty("trino.proxy.tls.trustStorePath");
        trustStorePass = env.getProperty("trino.proxy.tls.trustStorePass");

        setup();
        startServer();
    }


    private void setup() {
        server = new Server();
        server.setStopAtShutdown(true);
        ServerConnector connector = null;

        if (tlsEnabled) {
            File keystoreFile = new File(keystorePath);
            File trustStoreFile = new File(trustStorePath);

            TlsContextFactory tlsContextFactory = new TlsContextFactory();
            tlsContextFactory.setTrustAll(true);
            tlsContextFactory.setSslSessionTimeout(15);

            tlsContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
            tlsContextFactory.setKeyStorePassword(keystorePass);
            tlsContextFactory.setKeyManagerPassword(keystorePass);
            tlsContextFactory.setTrustStorePath(trustStoreFile.getAbsolutePath());
            tlsContextFactory.setTrustStorePassword(trustStorePass);

            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.setSecureScheme(HttpScheme.HTTPS.asString());
            httpsConfig.setOutputBufferSize(32768);

            SecureRequestCustomizer src = new SecureRequestCustomizer();
            src.setStsMaxAge(TimeUnit.SECONDS.toSeconds(2000));
            src.setStsIncludeSubDomains(true);
            httpsConfig.addCustomizer(src);
            connector =
                    new ServerConnector(
                            server,
                            new SslConnectionFactory(tlsContextFactory, HttpVersion.HTTP_1_1.asString()),
                            new HttpConnectionFactory(httpsConfig));
        } else {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setOutputBufferSize(32768);
            httpConfig.setIdleTimeout(-1);
            connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        }
        connector.setHost("0.0.0.0");
        connector.setPort(port);
        connector.setName("Trino Proxy");
        connector.setAccepting(true);
        server.addConnector(connector);

        ConnectHandler proxyConnectHandler = new ConnectHandler();
        server.setHandler(proxyConnectHandler);

        ServletHolder servletHolder = new ServletHolder("Trino Proxy Servlet", trinoProxyServlet);

        servletHolder.setInitParameter("proxyTo", "http://localhost:54321");
        servletHolder.setInitParameter("prefix", "/");
        servletHolder.setInitParameter("trustAll", "true");
        servletHolder.setInitParameter("preserveHost", "localhost");

        // Setup proxy servlet
        ServletContextHandler context =
                new ServletContextHandler(proxyConnectHandler, "/", ServletContextHandler.SESSIONS);
        context.addServlet(servletHolder, "/*");
        context.addFilter(RequestFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
    }

    private void startServer() throws Exception {
        server.start();
        LOG.info("Trino Proxy is running on {}...", port);
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
    }

    private static class TlsContextFactory extends SslContextFactory.Server {
        public TlsContextFactory() {
            super();
        }
    }
}
