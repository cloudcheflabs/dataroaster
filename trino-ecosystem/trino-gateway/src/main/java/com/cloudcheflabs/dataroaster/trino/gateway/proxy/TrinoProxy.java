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

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class TrinoProxy {

    private static Logger LOG = LoggerFactory.getLogger(TrinoProxy.class);

    public TrinoProxy() {
        Server server = new Server();
        server.setStopAtShutdown(true);
        ServerConnector connector = null;

        int httpPort = 18080;
        boolean tlsEnabled = true;
        String keystorePath = "/home/vagrant/cert-tool/work/keystore.jks";
        String keystorePass = "changeit";
        int httpsPort = 28080;
        if (tlsEnabled) {
            File keystoreFile = new File(keystorePath);

            TlsContextFactory tlsContextFactory = new TlsContextFactory();
            tlsContextFactory.setTrustAll(true);
            tlsContextFactory.setSslSessionTimeout(15);

            tlsContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
            tlsContextFactory.setKeyStorePassword(keystorePass);
            tlsContextFactory.setKeyManagerPassword(keystorePass);

            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.setSecureScheme(HttpScheme.HTTPS.asString());
            httpsConfig.setSecurePort(httpsPort);
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
            connector = new ServerConnector(server);
        }
        connector.setHost("0.0.0.0");
        connector.setPort(httpPort);
        connector.setName("Trino Proxy");
        connector.setAccepting(true);
        server.addConnector(connector);

        ConnectHandler proxyConnectHandler = new ConnectHandler();
        server.setHandler(proxyConnectHandler);

        TrinoProxyServlet trinoProxyServlet = new TrinoProxyServlet();

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

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("exception", e);
        }
    }

    public static class TlsContextFactory extends SslContextFactory.Server {
        public TlsContextFactory() {
            super();
        }
    }
}
