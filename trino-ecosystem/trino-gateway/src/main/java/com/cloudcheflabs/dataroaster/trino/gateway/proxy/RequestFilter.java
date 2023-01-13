package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

import static com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxyServlet.ATTR_BASIC_AUTHENTICATION;
import static com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxyServlet.HEADER_HTTP_AUTHORIZATION;

public class RequestFilter implements jakarta.servlet.Filter {

  private static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String authValue = req.getHeader(HEADER_HTTP_AUTHORIZATION);
    if(LOG.isDebugEnabled()) LOG.debug("authValue: [{}]", authValue);

    // set credentials to attribute to authenticate user later.
    if(authValue != null) {
      String[] tokens = authValue.split(" ");

      byte[] decodedBytes = Base64.getDecoder().decode(tokens[1]);
      String userPassword = new String(decodedBytes);
      // split the form of <user>:<password> into array.
      String[] userPassTokens = userPassword.split(":");
      String user = userPassTokens[0];
      String password = userPassTokens[1];
      if(LOG.isDebugEnabled()) LOG.debug("user: [{}], password: [{}]", user, password);

      req.setAttribute(ATTR_BASIC_AUTHENTICATION, new BasicAuthentication(user, password));
    }

    RequestWrapper requestWrapper = new RequestWrapper(req);
    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
    chain.doFilter(requestWrapper, responseWrapper);
  }


  public void destroy() {
    this.filterConfig = null;
  }
}
