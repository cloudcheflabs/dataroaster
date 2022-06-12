package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TrinoProxyServlet extends ProxyServlet.Transparent {

  private static Logger LOG = LoggerFactory.getLogger(TrinoProxyServlet.class);


  @Override
  protected void addProxyHeaders(HttpServletRequest request, Request proxyRequest) {
    super.addProxyHeaders(request, proxyRequest);
  }

  @Override
  protected String rewriteTarget(HttpServletRequest request) {
    String backendTrinoAddress = "http://localhost:8080";

    // TODO: get backend trino address.

    String target =
            backendTrinoAddress
                    + request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

    String source =
            request.getScheme()
                    + "://"
                    + request.getRemoteHost()
                    + ":"
                    + request.getServerPort()
                    + request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

    LOG.info("source: [{}], target: [{}]", source, target);

    return target;
  }

}
