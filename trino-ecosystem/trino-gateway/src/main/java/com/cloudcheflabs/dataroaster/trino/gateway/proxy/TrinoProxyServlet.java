package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.RandomUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TrinoProxyServlet extends ProxyServlet.Transparent implements InitializingBean {

  private static Logger LOG = LoggerFactory.getLogger(TrinoProxyServlet.class);

  public static final String ATTR_BASIC_AUTHENTICATION = "BasicAuthentication";

  public static final String HEADER_TRINO_USER = "X-Trino-User";
  public static final String HEADER_HTTP_AUTHORIZATION = "Authorization";

  @Autowired
  private Environment env;

  @Autowired
  @Qualifier("usersServiceImpl")
  private UsersService usersService;

  @Autowired
  @Qualifier("clusterGroupServiceImpl")
  private ClusterGroupService clusterGroupService;

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
        throw new IllegalStateException("Password missing...");
      } else {
        // get any trino clusters registered if authentication is not necessary.
        List<Cluster> clusterList = new ArrayList<>();
        List<ClusterGroup> clusterGroupList = clusterGroupService.findAll();
        for(ClusterGroup clusterGroup : clusterGroupList) {
          for(Cluster cluster : clusterGroup.getClusterSet()) {
            if(cluster.isActivated()) {
              clusterList.add(cluster);
            }
          }
        }

        if(clusterList.size() == 0) {
          throw new IllegalStateException("There is no cluster for routing");
        }
        // choose one of the clusters through cluster randomization.
        Cluster chosedCluster = RandomUtils.randomize(clusterList);
        String backendTrinoAddress = chosedCluster.getUrl();
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

      // authenticate user, and get target trino address.
      Users users = usersService.findOne(user);
      if(users == null) {
        throw new IllegalStateException("user [" + user + "] does not exist!");
      }
      String bcryptEncodedPassword = users.getPassword();
      boolean isMatched = BCryptUtils.isMatched(password, bcryptEncodedPassword);
      if(isMatched) {
        ClusterGroup clusterGroup = users.getClusterGroup();
        Set<Cluster> clusterSet = clusterGroup.getClusterSet();
        if(clusterSet.size() == 0) {
          throw new IllegalStateException("There is no cluster in the group [" + clusterGroup.getGroupName() + "]");
        }

        List<Cluster> clusterList = new ArrayList<>();
        for(Cluster cluster : clusterSet) {
          if(cluster.isActivated()) {
            clusterList.add(cluster);
          }
        }
        if(clusterList.size() == 0) {
          throw new IllegalStateException("There is no cluster for routing");
        }

        // choose one of the clusters through cluster randomization.
        Cluster chosedCluster = RandomUtils.randomize(clusterList);
        String backendTrinoAddress = chosedCluster.getUrl();
        String target =
                backendTrinoAddress
                        + request.getRequestURI()
                        + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        LOG.info("source: [{}], target: [{}]", source, target);

        return target;
      } else {
        throw new IllegalStateException("User Authentication Failed...");
      }
    }
  }

  @Override
  protected void onResponseContent(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Response proxyResponse,
                                   byte[] buffer,
                                   int offset,
                                   int length,
                                   Callback callback) {


    if(LOG.isDebugEnabled()) {
      LOG.debug("onResponseContent buffer: [{}]", new String(buffer));
      for (String header : response.getHeaderNames()) {
        LOG.debug("header [{}]: [{}]", header, response.getHeader(header));
      }
    }

    super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
  }
}
