package com.cloudcheflabs.dataroaster.trino.gateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxyServlet.HEADER_HTTP_AUTHORIZATION;

public class RequestWrapper extends HttpServletRequestWrapper {

  private static Logger LOG = LoggerFactory.getLogger(RequestWrapper.class);

  public RequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
  }


  /**
   * reconstruct headers removing headers like 'Authorization'
   * to forward queries to downstream trino which will not do authentication.
   *
   * @return
   */
  @Override
  public Enumeration<String> getHeaderNames() {
    List<String> names = Collections.list(super.getHeaderNames());

    List<String> filteredHeaderNames = new ArrayList<>();

    // remove Authorization header.
    for(String name : names) {
      if(!name.equals(HEADER_HTTP_AUTHORIZATION)) {
        filteredHeaderNames.add(name);
      } else {
        if(LOG.isDebugEnabled()) {
          LOG.debug("header [{}}] removed to forward queries to downstream trino which will not do authentication", name);
        }
      }
    }

    return Collections.enumeration(filteredHeaderNames);
  }
}
