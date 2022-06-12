package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TrinoProxyServlet extends ProxyServlet.Transparent implements InitializingBean {

  private static Logger LOG = LoggerFactory.getLogger(TrinoProxyServlet.class);

  public static final String ATTR_BASIC_AUTHENTICATION = "BasicAuthentication";

  public static final String HEADER_TRINO_USER = "X-Trino-User";
  public static final String HEADER_HTTP_AUTHORIZATION = "Authorization";

  @Autowired
  private Environment env;

  private boolean authenticationNecessary;

  @Override
  public void afterPropertiesSet() throws Exception {
    authenticationNecessary = Boolean.valueOf(env.getProperty("trino.proxy.authentication"));
    LOG.info("authenticationNecessary: [{}]", authenticationNecessary);
  }

  @Override
  protected void addProxyHeaders(HttpServletRequest request, Request proxyRequest) {
    super.addProxyHeaders(request, proxyRequest);
  }

  @Override
  protected String rewriteTarget(HttpServletRequest request) {
    String source =
            request.getScheme()
                    + "://"
                    + request.getRemoteHost()
                    + ":"
                    + request.getServerPort()
                    + request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

    Object basicAuthObj = request.getAttribute(ATTR_BASIC_AUTHENTICATION);
    if(basicAuthObj == null) {
      String user = request.getHeader(HEADER_TRINO_USER);
      LOG.warn("User [{}] does not have any Basic Authentication Attribute!", user);

      // get any trino clusters registered if authentication is not necessary, otherwise return exception.
      if(authenticationNecessary) {
        LOG.error("User [{}] must have password to be authenticated!", user);
        return null;
      } else
      {
        // TODO: get any trino clusters registered if authentication is not necessary.
        String backendTrinoAddress = "http://localhost:8080";
        String target =
                backendTrinoAddress
                        + request.getRequestURI()
                        + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        LOG.info("source: [{}], target: [{}]", source, target);
        return target;
      }
    } else {
      BasicAuthentication basicAuthentication = (BasicAuthentication) basicAuthObj;
      String user = basicAuthentication.getUser();
      String password = basicAuthentication.getPassword();
     // LOG.info("user: [{}], password: [{}] from attribute", user, password);
      // TODO: authenticate user, and get target trino address.

      String backendTrinoAddress = "http://localhost:8080";

      // TODO: get backend trino address.

      String target =
              backendTrinoAddress
                      + request.getRequestURI()
                      + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

      LOG.info("source: [{}], target: [{}]", source, target);

      return target;
    }
  }
}
