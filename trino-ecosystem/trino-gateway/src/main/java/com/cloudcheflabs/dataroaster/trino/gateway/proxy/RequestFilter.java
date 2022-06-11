package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Enumeration;

public class RequestFilter implements Filter {

  private static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);
  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  public void destroy() {
    this.filterConfig = null;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
    Enumeration<String> headers = requestWrapper.getHeaderNames();
    while(headers.hasMoreElements()) {
      String header = headers.nextElement();
      String headerValue = requestWrapper.getHeader(header);
      Enumeration<String> headerValues = requestWrapper.getHeaders(header);
      LOG.info("header: [{}], value: [{}], values: [{}]", header, headerValue, headerValues.toString());
    }
    String body = requestWrapper.getBody();
    LOG.info("body: [{}]", body);

    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
    chain.doFilter(requestWrapper, responseWrapper);
  }
}
