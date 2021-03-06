package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoResponse;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.GzipUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.RandomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
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

  @Autowired
  @Qualifier("trinoResponseRedisCache")
  private CacheService<TrinoResponse> trinoResponseRedisCache;

  @Autowired
  private KubernetesClient kubernetesClient;

  private boolean authenticationNecessary;
  private String publicEndpoint;

  @Override
  public void afterPropertiesSet() throws Exception {
    authenticationNecessary = Boolean.valueOf(env.getProperty("trino.proxy.authentication"));
    LOG.info("authenticationNecessary: [{}]", authenticationNecessary);
    publicEndpoint = env.getProperty("trino.proxy.publicEndpoint");
    LOG.info("publicEndpoint: [{}]", publicEndpoint);
  }

  @Override
  protected void addProxyHeaders(HttpServletRequest request, Request proxyRequest) {
    super.addProxyHeaders(request, proxyRequest);
  }

  private String getQueryId(String uri) {
    List<String> uriPrefixList = Arrays.asList(
            "/v1/statement/queued/",
            "/v1/statement/executing/"
    );

    for(String uriPrefix : uriPrefixList) {
      if (uri.contains(uriPrefix)) {
        uri = uri.replaceAll(uriPrefix, "");

        String[] tokens = uri.split("/");
        return tokens[0];
      }
    }
    return null;
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


    String queryId = getQueryId(request.getRequestURI());
    LOG.info("queryId: {}", queryId);
    if(queryId != null) {
      TrinoResponse trinoResponse = trinoResponseRedisCache.get(queryId, TrinoResponse.class);
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


  @Override
  protected void onResponseContent(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Response proxyResponse,
                                   byte[] buffer,
                                   int offset,
                                   int length,
                                   Callback callback) {


    //
    // retrieve query id and nextUri from response buffer, and save them to cache.
    // replace the hostname of nextUri with trino gateway ingress host name.
    //



    // print header.
    for (String header : response.getHeaderNames()) {
      if(LOG.isDebugEnabled()) LOG.debug("header [{}]: [{}]", header, response.getHeader(header));
    }

    if(LOG.isDebugEnabled()) LOG.debug("buffer size: {}", buffer.length);
    if(LOG.isDebugEnabled()) LOG.debug("offset: {}", offset);
    if(LOG.isDebugEnabled()) LOG.debug("length: {}", length);

    String contentLength = response.getHeader("Content-Length");
    if(LOG.isDebugEnabled()) LOG.debug("contentLength: {}", contentLength);

    String contentEncoding = response.getHeader("Content-Encoding");
    if(LOG.isDebugEnabled()) LOG.debug("contentEncoding: {}", contentEncoding);

    String jsonResponse = null;
    if(contentEncoding != null && contentEncoding.toLowerCase().equals("gzip")) {
      jsonResponse = GzipUtils.decompressGzip(buffer);
    } else {
      jsonResponse = new String(buffer);
    }
    if(LOG.isDebugEnabled()) LOG.debug("jsonResponse: {}", jsonResponse);

    // save response to cache.
    Map<String, Object> responseMap = JsonUtils.toMap(new ObjectMapper(), jsonResponse);
    String id = (String) responseMap.get("id");
    if(LOG.isDebugEnabled()) LOG.debug("id: {}", id);
    String nextUri = (responseMap.containsKey("nextUri")) ? (String) responseMap.get("nextUri") : null;
    if(LOG.isDebugEnabled()) LOG.debug("nextUri: {}", nextUri);

    String infoUri = (responseMap.containsKey("infoUri")) ? (String) responseMap.get("infoUri") : null;
    if(LOG.isDebugEnabled()) LOG.debug("infoUri: {}", infoUri);

    TrinoResponse trinoResponse = new TrinoResponse();
    trinoResponse.setId(id);
    trinoResponse.setNextUri(nextUri);
    trinoResponse.setInfoUri(infoUri);

    trinoResponseRedisCache.set(id, trinoResponse);

    // change nextUri.
    if(nextUri != null) {
      // replace the backend trino hostname with proxy public endpoint.
      String newNextUri = replaceUri(nextUri, publicEndpoint);
      String newInfoUri = replaceUri(infoUri, publicEndpoint);
      responseMap.put("nextUri", newNextUri);
      responseMap.put("infoUri", newInfoUri);
      String newJsonReponse = JsonUtils.toJson(responseMap);
      if(LOG.isDebugEnabled()) LOG.debug("newJsonReponse: {}", newJsonReponse);

      // gzip compressed json.
      if(contentEncoding != null && contentEncoding.toLowerCase().equals("gzip")) {
        buffer = GzipUtils.compressStringInGzip(newJsonReponse);
      } else {
        buffer = newJsonReponse.getBytes();
      }

      length = buffer.length;
      if(LOG.isDebugEnabled()) LOG.debug("new length: {}", length);

      // set new content length.
      response.setHeader("Content-Length", String.valueOf(length));
    }

    super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
  }


  private String replaceUri(String uri, String hostName) {
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
