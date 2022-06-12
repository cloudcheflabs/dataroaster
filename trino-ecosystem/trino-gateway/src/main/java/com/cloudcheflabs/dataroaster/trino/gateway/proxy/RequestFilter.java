package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
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
    HttpServletRequest req = (HttpServletRequest) request;
    String authValue = req.getHeader("Authorization");
    //LOG.info("authValue: [{}]", authValue);

    // TODO: do basic authentication.
    if(authValue != null) {
      String[] tokens = authValue.split(" ");

      byte[] decodedBytes = Base64.getDecoder().decode(tokens[1]);
      String userPassword = new String(decodedBytes);
      // split the form of <user>:<password> into array.
      String[] userPassTokens = userPassword.split(":");
      String user = userPassTokens[0];
      String password = userPassTokens[1];
      //LOG.info("user: [{}], password: [{}]", user, password);
    }

    RequestWrapper requestWrapper = new RequestWrapper(req);

    if(LOG.isDebugEnabled()) {
      // print all headers without Authorization header.
      Enumeration<String> headers = requestWrapper.getHeaderNames();
      while (headers.hasMoreElements()) {
        String header = headers.nextElement();
        String headerValue = requestWrapper.getHeader(header);
        Enumeration<String> headerValues = requestWrapper.getHeaders(header);
        LOG.debug("header: [{}], value: [{}], values: [{}]", header, headerValue, JsonUtils.toJson(new ObjectMapper(), Collections.list(headerValues)));
      }

      String body = requestWrapper.getBody();
      LOG.debug("body: [{}]", body);
    }

    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
    chain.doFilter(requestWrapper, responseWrapper);
  }


  public void destroy() {
    this.filterConfig = null;
  }
}
