package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

public class RequestFilter implements jakarta.servlet.Filter {

  private static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);
  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);

    Enumeration<String> headers = requestWrapper.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      String headerValue = requestWrapper.getHeader(header);
      Enumeration<String> headerValues = requestWrapper.getHeaders(header);
      LOG.info("header: [{}], value: [{}], values: [{}]", header, headerValue, JsonUtils.toJson(new ObjectMapper(), Collections.list(headerValues)));
    }

    String body = requestWrapper.getBody();
    LOG.info("body: [{}]", body);

    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
    chain.doFilter(requestWrapper, responseWrapper);
  }


  public void destroy() {
    this.filterConfig = null;
  }
}
