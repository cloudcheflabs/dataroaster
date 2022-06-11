package com.cloudcheflabs.dataroaster.trino.gateway.proxy;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class TrinoProxy {

    private static Logger LOG = LoggerFactory.getLogger(TrinoProxy.class);

    public TrinoProxy() {
        Server server = new Server();
        server.setStopAtShutdown(true);
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("0.0.0.0");
        // TODO: port configurable.
        connector.setPort(18080);
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
}
