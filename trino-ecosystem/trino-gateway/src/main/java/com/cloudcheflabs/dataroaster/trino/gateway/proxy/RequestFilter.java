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
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

public class RequestFilter implements jakarta.servlet.Filter {

  private static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);
  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String user = req.getHeader("X-Trino-User");
    String authValue = req.getHeader("Authorization");
    LOG.info("user: [{}], authValue: [{}]", user, authValue);

    // TODO: do basic authentication.
    if(user != null && authValue != null) {
      String[] tokens = authValue.split(" ");

      byte[] decodedBytes = Base64.getDecoder().decode(tokens[1]);
      String password = new String(decodedBytes);
      LOG.info("user: [{}], password: [{}]", user, password);
    }

    RequestWrapper requestWrapper = new RequestWrapper(req);

    // print all headers without Authorization header.
    Enumeration<String> headers = requestWrapper.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      String headerValue = requestWrapper.getHeader(header);
      Enumeration<String> headerValues = requestWrapper.getHeaders(header);
      LOG.info("header: [{}], value: [{}], values: [{}]", header, headerValue, JsonUtils.toJson(new ObjectMapper(), Collections.list(headerValues)));
    }

    // remove

    String body = requestWrapper.getBody();
    LOG.info("body: [{}]", body);

    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
    chain.doFilter(requestWrapper, responseWrapper);
  }


  public void destroy() {
    this.filterConfig = null;
  }
}
