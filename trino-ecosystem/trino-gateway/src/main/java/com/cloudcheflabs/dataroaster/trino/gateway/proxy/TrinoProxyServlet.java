package com.cloudcheflabs.dataroaster.trino.gateway.proxy;


import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.component.TrinoActiveQueryCountUpdater;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.BasicAuthentication;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.ClusterWithActiveQueryCount;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoResponse;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.DeflateUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.GzipUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.RandomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.Callback;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @Qualifier("trinoActiveQueryCountCacheServiceImpl")
    private CacheService<TrinoActiveQueryCount> trinoActiveQueryCountUpdaterCacheService;

    @Autowired
    private KubernetesClient kubernetesClient;

    private boolean authenticationNecessary;
    private String publicEndpoint;

    private ObjectMapper mapper = new ObjectMapper();

    private ConcurrentHashMap<Long, NotCompletedResponseBuffer> tempResponseBufferMap = new ConcurrentHashMap<>();

    private static class NotCompletedResponseBuffer {
        private ByteArrayOutputStream os;
        private long threadId;
        private long startTime;

        public NotCompletedResponseBuffer(ByteArrayOutputStream os,
                                          long threadId,
                                          long startTime) {
            this.os = os;
            this.threadId = threadId;
            this.startTime = startTime;
        }

        public ByteArrayOutputStream getOs() {
            return os;
        }

        public long getThreadId() {
            return threadId;
        }

        public long getStartTime() {
            return startTime;
        }
    }

    private static class NotCompletedResponseBufferExpirationChecker implements Runnable {

        private ConcurrentHashMap<Long, NotCompletedResponseBuffer> tempResponseBufferMap;

        public NotCompletedResponseBufferExpirationChecker(ConcurrentHashMap<Long, NotCompletedResponseBuffer> tempResponseBufferMap) {
            this.tempResponseBufferMap = tempResponseBufferMap;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    for (Long threadId : tempResponseBufferMap.keySet()) {
                        NotCompletedResponseBuffer notCompletedResponseBuffer = tempResponseBufferMap.get(threadId);
                        long startTime = notCompletedResponseBuffer.getStartTime();
                        long endTime = DateTimeUtils.currentTimeMillis();
                        // if 30seconds elapsed, remove buffer.
                        if ((endTime - startTime) > 30 * 1000) {
                            tempResponseBufferMap.remove(threadId);
                            LOG.info("notCompletedResponseBuffer with thread id [{}] removed from temp buffer map.", threadId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // pause.
                TrinoActiveQueryCountUpdater.pause(30 * 1000);
            }
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        authenticationNecessary = Boolean.valueOf(env.getProperty("trino.proxy.authentication"));
        LOG.info("authenticationNecessary: [{}]", authenticationNecessary);
        publicEndpoint = env.getProperty("trino.proxy.publicEndpoint");
        LOG.info("publicEndpoint: [{}]", publicEndpoint);

        // run temp response buffer checker.
        new Thread(new NotCompletedResponseBufferExpirationChecker(tempResponseBufferMap)).start();
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
            LOG.info("header [{}]: [{}]", header, response.getHeader(header));
        }

        LOG.info("buffer size: {}", buffer.length);
        LOG.info("offset: {}", offset);
        LOG.info("length: {}", length);

        String contentLength = response.getHeader("Content-Length");
        LOG.info("contentLength: {}", contentLength);

        String contentEncoding = response.getHeader("Content-Encoding");
        LOG.info("contentEncoding: {}", contentEncoding);

        long threadId = Thread.currentThread().getId();
        LOG.info("thread id: {}, request id: {}", threadId, getRequestId(request));

        if(!tempResponseBufferMap.contains(threadId)) {
            tempResponseBufferMap.put(threadId, new NotCompletedResponseBuffer(
                    new ByteArrayOutputStream(),
                    threadId,
                    DateTimeUtils.currentTimeMillis()
            ));
        }
        NotCompletedResponseBuffer notCompletedResponseBuffer = tempResponseBufferMap.get(threadId);

        Map<String, Object> responseMap = null;
        if (contentEncoding != null && contentEncoding.toLowerCase().equals("gzip")) {
            byte[] decompressedBytes = null;
            try {
                decompressedBytes = GzipUtils.decompress(buffer);
                LOG.info("gzip decompression success...");
            } catch (Exception e) {
                super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
                LOG.info("portion of gzip data...[{}]", threadId);
                notCompletedResponseBuffer.getOs().write(buffer, offset, length);
                try {
                    byte[] accumulatedBytes = notCompletedResponseBuffer.getOs().toByteArray();
                    LOG.info("accumulatedBytes: {}", accumulatedBytes.length);
                    byte[] tempDecompressedBytes = GzipUtils.decompress(accumulatedBytes);
                    String jsonResponse = new String(tempDecompressedBytes);
                    LOG.info("ready to convert to map...");
                    responseMap = JsonUtils.toMap(mapper, jsonResponse);
                    LOG.info("map conversion done...");
                } catch (Exception ex) {
                    LOG.info("not json format...");
                    return;
                }
            }

            if(decompressedBytes != null) {
                try {
                    String jsonResponse = new String(decompressedBytes);
                    LOG.info("ready to convert to map...");
                    responseMap = JsonUtils.toMap(mapper, jsonResponse);
                    LOG.info("map conversion done...");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        } else {
            LOG.info("content encoding not gzip...[{}]", threadId);
            notCompletedResponseBuffer.getOs().write(buffer, offset, length);
            try {
                String jsonResponse = new String(notCompletedResponseBuffer.getOs().toByteArray());
                LOG.info("ready to convert to map...");
                responseMap = JsonUtils.toMap(mapper, jsonResponse);
                LOG.info("map conversion done...");
            } catch (Exception ex) {
                LOG.info("not json format...");
                super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
                return;
            }
        }

        // remove temp buffer.
        tempResponseBufferMap.remove(threadId);

        LOG.info("jsonResponse: {}", JsonUtils.toJson(responseMap));

        // save response to cache.
        String id = (String) responseMap.get("id");
        LOG.info("id: {}", id);
        String nextUri = (responseMap.containsKey("nextUri")) ? (String) responseMap.get("nextUri") : null;
        LOG.info("nextUri: {}", nextUri);

        String infoUri = (responseMap.containsKey("infoUri")) ? (String) responseMap.get("infoUri") : null;
        LOG.info("infoUri: {}", infoUri);

        String partialCancelUri = (responseMap.containsKey("partialCancelUri")) ? (String) responseMap.get("partialCancelUri") : null;
        LOG.info("partialCancelUri: {}", partialCancelUri);

        TrinoResponse trinoResponse = new TrinoResponse();
        trinoResponse.setId(id);
        trinoResponse.setNextUri(nextUri);
        trinoResponse.setInfoUri(infoUri);
        if(partialCancelUri != null) {
            trinoResponse.setPartialCancelUri(partialCancelUri);
        }

        // cache nextUri.
        trinoResponseRedisCache.set(id, trinoResponse);

        // change nextUri.
        if (nextUri != null) {
            LOG.info("nextUri is not null!");

            // replace the backend trino hostname with proxy public endpoint.
            String newNextUri = replaceUri(nextUri, publicEndpoint);
            String newInfoUri = replaceUri(infoUri, publicEndpoint);
            responseMap.put("nextUri", newNextUri);
            responseMap.put("infoUri", newInfoUri);
            if(partialCancelUri != null) {
                String newPartialCancelUri = replaceUri(partialCancelUri, publicEndpoint);
                responseMap.put("partialCancelUri", newPartialCancelUri);
            }

            String newJsonReponse = JsonUtils.toJson(responseMap);
            LOG.info("newJsonReponse: {}", newJsonReponse);

            // gzip compressed json.
            if (contentEncoding != null && contentEncoding.toLowerCase().equals("gzip")) {
                buffer = GzipUtils.compressStringInGzip(newJsonReponse);
                LOG.info("compress json to gzip...");
            } else {
                buffer = newJsonReponse.getBytes();
                LOG.info("just get bytes...");
            }

            length = buffer.length;
            if (LOG.isDebugEnabled()) LOG.debug("new length: {}", length);

            // set new content length.
            response.setHeader("Content-Length", String.valueOf(length));
        }

        super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
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
