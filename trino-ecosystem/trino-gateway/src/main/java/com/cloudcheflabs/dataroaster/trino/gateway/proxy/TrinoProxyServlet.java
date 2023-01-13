package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.ClusterWithActiveQueryCount;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
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
import org.eclipse.jetty.proxy.AfterContentTransformer;
import org.eclipse.jetty.proxy.AsyncMiddleManServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Component
public class TrinoProxyServlet extends AsyncMiddleManServlet.Transparent implements InitializingBean {

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
    @Qualifier("trinoActiveQueryCountCacheServiceImpl")
    private CacheService<TrinoActiveQueryCount> trinoActiveQueryCountUpdaterCacheService;

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

        for (String uriPrefix : uriPrefixList) {
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
        if (queryId != null) {
            TrinoResponse trinoResponse = trinoResponseRedisCache.get(queryId, TrinoResponse.class);
            if (trinoResponse != null) {
                String target = trinoResponse.getNextUri();
                if (target != null) {
                    LOG.info("source: [{}], target: [{}]", source, target);
                    return target;
                }
            }
        }

        Object basicAuthObj = request.getAttribute(ATTR_BASIC_AUTHENTICATION);
        if (basicAuthObj == null) {
            String user = request.getHeader(HEADER_TRINO_USER);
            LOG.warn("User [{}] does not have any Basic Authentication Attribute!", user);

            // get any trino clusters registered if authentication is not necessary, otherwise return exception.
            if (authenticationNecessary) {
                LOG.error("User [{}] must have password to be authenticated!", user);
                throw new IllegalStateException("Password missing...");
            } else {
                // get any trino clusters registered if authentication is not necessary.
                List<Cluster> clusterList = new ArrayList<>();
                List<ClusterGroup> clusterGroupList = clusterGroupService.findAll();
                for (ClusterGroup clusterGroup : clusterGroupList) {
                    for (Cluster cluster : clusterGroup.getClusterSet()) {
                        if (cluster.isActivated()) {
                            clusterList.add(cluster);
                        }
                    }
                }

                if (clusterList.size() == 0) {
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
            if (users == null) {
                throw new IllegalStateException("user [" + user + "] does not exist!");
            }
            String bcryptEncodedPassword = users.getPassword();
            boolean isMatched = BCryptUtils.isMatched(password, bcryptEncodedPassword);
            if (isMatched) {
                ClusterGroup clusterGroup = users.getClusterGroup();
                Set<Cluster> clusterSet = clusterGroup.getClusterSet();
                if (clusterSet.size() == 0) {
                    throw new IllegalStateException("There is no cluster in the group [" + clusterGroup.getGroupName() + "]");
                }

                List<Cluster> clusterList = new ArrayList<>();
                for (Cluster cluster : clusterSet) {
                    if (cluster.isActivated()) {
                        clusterList.add(cluster);
                    }
                }
                if (clusterList.size() == 0) {
                    throw new IllegalStateException("There is no cluster for routing");
                }

                List<ClusterWithActiveQueryCount> clusterWithActiveQueryCounts = new ArrayList<>();
                for(Cluster cluster : clusterList) {
                    TrinoActiveQueryCount trinoActiveQueryCount = trinoActiveQueryCountUpdaterCacheService.get(cluster.getClusterName(), TrinoActiveQueryCount.class);
                    if(trinoActiveQueryCount != null) {
                        clusterWithActiveQueryCounts.add(new ClusterWithActiveQueryCount(cluster, trinoActiveQueryCount));
                    }
                }

                // sort by trino active query count.
                Collections.sort(clusterWithActiveQueryCounts, new Comparator<ClusterWithActiveQueryCount>() {
                    @Override
                    public int compare(ClusterWithActiveQueryCount o1, ClusterWithActiveQueryCount o2) {
                        return o1.getTrinoActiveQueryCount().getCount() - o2.getTrinoActiveQueryCount().getCount();
                    }
                });

                // choose one cluster with lowest active query count.
                Cluster chosenCluster = null;
                if(clusterWithActiveQueryCounts.size() > 0) {
                    ClusterWithActiveQueryCount chosenClusterWithActiveQueryCount = clusterWithActiveQueryCounts.get(0);
                    chosenCluster = chosenClusterWithActiveQueryCount.getCluster();
                    LOG.info("chosen cluster: {}, active query count: {}", chosenCluster.getClusterName(),
                            chosenClusterWithActiveQueryCount.getTrinoActiveQueryCount().getCount());
                } else {
                    chosenCluster = RandomUtils.randomize(clusterList);
                }

                String backendTrinoAddress = chosenCluster.getUrl();
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
    protected ContentTransformer newServerResponseContentTransformer(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse)
    {
        return new TrinoResponseContentTransformer(
                clientRequest,
                proxyResponse,
                serverResponse,
                trinoResponseRedisCache,
                publicEndpoint
        );
    }

    private static class TrinoResponseContentTransformer extends AfterContentTransformer
    {
        private ObjectMapper mapper = new ObjectMapper();
        private CacheService<TrinoResponse> trinoResponseRedisCache;
        private String publicEndpoint;
        private HttpServletRequest clientRequest;
        private HttpServletResponse proxyResponse;
        private Response serverResponse;

        public TrinoResponseContentTransformer(HttpServletRequest clientRequest,
                                               HttpServletResponse proxyResponse,
                                               Response serverResponse,
                                               CacheService<TrinoResponse> trinoResponseRedisCache,
                                               String publicEndpoint) {
            this.clientRequest = clientRequest;
            this.proxyResponse = proxyResponse;
            this.serverResponse = serverResponse;
            this.trinoResponseRedisCache = trinoResponseRedisCache;
            this.publicEndpoint = publicEndpoint;
        }

        @Override
        public boolean transform(Source source, Sink sink) throws IOException {
            InputStream input = source.getInputStream();

            byte[] inputBytes = input.readAllBytes();
            boolean isGzip = false;

            // decompress
            String jsonResponse = null;
            try {
                byte[] decompressedBytes = GzipUtils.decompress(inputBytes);
                jsonResponse = new String(decompressedBytes);
                isGzip = true;
            } catch (Exception e) {
                // not gzipped content.
                jsonResponse = new String(inputBytes);
            }

            Map<String, Object> responseMap = null;
            try {
                responseMap = JsonUtils.toMap(mapper, jsonResponse);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            // save nextUri, etc to cache.
            String id = (String) responseMap.get("id");
            String nextUri = (responseMap.containsKey("nextUri")) ? (String) responseMap.get("nextUri") : null;
            String infoUri = (responseMap.containsKey("infoUri")) ? (String) responseMap.get("infoUri") : null;
            String partialCancelUri = (responseMap.containsKey("partialCancelUri")) ? (String) responseMap.get("partialCancelUri") : null;

            TrinoResponse trinoResponse = new TrinoResponse();
            trinoResponse.setId(id);
            trinoResponse.setNextUri(nextUri);
            trinoResponse.setInfoUri(infoUri);
            trinoResponse.setPartialCancelUri(partialCancelUri);

            // cache nextUri.
            trinoResponseRedisCache.set(id, trinoResponse);

            byte[] buffer = null;
            // replace host names of nextUri, infoUri and partialCancelUri with trino gateway hostname.
            if (nextUri != null || infoUri != null || partialCancelUri != null) {
                // replace the backend trino hostname with proxy public endpoint.
                if(nextUri != null) {
                    String newNextUri = replaceUri(nextUri, publicEndpoint);
                    responseMap.put("nextUri", newNextUri);
                }
                if(infoUri != null) {
                    String newInfoUri = replaceUri(infoUri, publicEndpoint);
                    responseMap.put("infoUri", newInfoUri);
                }
                if(partialCancelUri != null) {
                    String newPartialCancelUri = replaceUri(partialCancelUri, publicEndpoint);
                    responseMap.put("partialCancelUri", newPartialCancelUri);
                }
                String newJsonReponse = JsonUtils.toJson(responseMap);
                // gzip compressed json.
                if (isGzip) {
                    // compress new constructed json in gzip.
                    buffer = GzipUtils.compressStringInGzip(newJsonReponse);
                } else {
                    // not gzipped encoding.
                    buffer = newJsonReponse.getBytes();
                }
            } else {
                if (isGzip) {
                    // compress new constructed json in gzip.
                    buffer = GzipUtils.compressStringInGzip(jsonResponse);
                } else {
                    // not gzipped encoding.
                    buffer = jsonResponse.getBytes();
                }
            }
            int length = buffer.length;
            // set new content length.
            proxyResponse.setHeader("Content-Length", String.valueOf(length));

            OutputStream output = sink.getOutputStream();
            output.write(buffer);
            return true;
        }

        private String replaceUri(String uri, String hostName) {
            String[] tokens = uri.split("/");

            int count = 0;
            StringBuffer sb = new StringBuffer();
            for (String token : tokens) {
                if (count > 2) {
                    sb.append("/").append(token);
                }
                count++;
            }
            return hostName + sb.toString();
        }
    }
}
