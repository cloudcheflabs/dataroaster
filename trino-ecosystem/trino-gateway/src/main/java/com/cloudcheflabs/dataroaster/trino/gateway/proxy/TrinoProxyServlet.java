package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.RandomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.Serializable;
import java.util.*;

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


    String trinoTxId = request.getHeader("X-Trino-Transaction-Id");
    LOG.info("trinoTxId: {}", trinoTxId);
    if(trinoTxId != null) {
      TrinoResponse trinoResponse = (trinoReponseCache.containsKey(trinoTxId)) ? trinoReponseCache.get(trinoTxId) : null;
      if(trinoResponse != null) {
        String target = trinoResponse.getNextUri();
        if (target != null) {
          LOG.info("source: [{}], target: [{}]", source, target);
          return target;
        }
      }
    }


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


  // TODO: this is just temp cache.
  public Map<String, TrinoResponse> trinoReponseCache = new HashMap<>();

  public static class TrinoResponse implements Serializable {
    private String id;
    private String nextUri;

    private String infoUri;

    public String getInfoUri() {
      return infoUri;
    }

    public void setInfoUri(String infoUri) {
      this.infoUri = infoUri;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getNextUri() {
      return nextUri;
    }

    public void setNextUri(String nextUri) {
      this.nextUri = nextUri;
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

    String jsonResponse = new String(buffer);

    if(LOG.isInfoEnabled()) {
      LOG.info("onResponseContent buffer: ", jsonResponse);
      for (String header : response.getHeaderNames()) {
        LOG.info("header [{}]: [{}]", header, response.getHeader(header));
      }
    }

    // save response to cache.
    Map<String, Object> responseMap = JsonUtils.toMap(new ObjectMapper(), jsonResponse);
    String id = (String) responseMap.get("id");
    LOG.info("id: {}", id);
    String nextUri = (responseMap.containsKey("nextUri")) ? (String) responseMap.get("nextUri") : null;
    LOG.info("nextUri: {}", nextUri);

    String infoUri = (responseMap.containsKey("infoUri")) ? (String) responseMap.get("infoUri") : null;
    LOG.info("infoUri: {}", infoUri);

    TrinoResponse trinoResponse = new TrinoResponse();
    trinoResponse.setId(id);
    trinoResponse.setNextUri(nextUri);
    trinoReponseCache.put(id, trinoResponse);

    // change nextUri.
    if(nextUri != null) {

      String newNextUri = replaceUri(nextUri);
      String newInfoUri = replaceUri(infoUri);
      responseMap.put("nextUri", newNextUri);
      responseMap.put("infoUri", newInfoUri);
      String newJsonReponse = JsonUtils.toJson(responseMap);
      LOG.info("newJsonReponse: ", newJsonReponse);
      buffer = newJsonReponse.getBytes();
    }


    super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
  }

  private String replaceUri(String uri) {
    String hostName = "https://trino-gateway-proxy-test.cloudchef-labs.com";


    String[] tokens = uri.split("/");

    int count = 0;
    StringBuffer sb = new StringBuffer();
    for(String token : tokens) {
      if(count > 2) {
        sb.append("/").append(token);
      }
      count++;
    }
    return hostName + sb.toString();
  }
}
