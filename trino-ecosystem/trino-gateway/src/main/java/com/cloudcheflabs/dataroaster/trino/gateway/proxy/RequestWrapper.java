package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class RequestWrapper extends HttpServletRequestWrapper {

  private static Logger LOG = LoggerFactory.getLogger(RequestWrapper.class);

  private byte[] content;

  public static void copy(InputStream in, OutputStream out) throws IOException {

    byte[] buffer = new byte[1024];
    while (true) {
      int bytesRead = in.read(buffer);
      if (bytesRead == -1) {
        break;
      }
      out.write(buffer, 0, bytesRead);
    }
  }

  public RequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    ByteArrayOutputStream bodyInOutputStream = new ByteArrayOutputStream();
    copy(request.getInputStream(), bodyInOutputStream);
    content = bodyInOutputStream.toByteArray();
  }


  public String getBody() {
    return new String(content);
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
      if(!name.equals("Authorization")) {
        filteredHeaderNames.add(name);
      } else {
        if(LOG.isDebugEnabled()) {
          LOG.debug("header [{}}] removed to forward queries to downstream trino which will not do authentication", name);
        }
      }
    }

    return Collections.enumeration(filteredHeaderNames);
  }



  @Override
  public ServletInputStream getInputStream() throws IOException {
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {}

      public int read() throws IOException {
        return byteArrayInputStream.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(this.getInputStream()));
  }
}
