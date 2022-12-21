package com.cloudcheflabs.dataroaster.trino.gateway.service;

import com.cloudcheflabs.dataroaster.trino.gateway.api.service.TrinoActiveQueryCountRestService;
import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.ResponseHandler;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
import com.cloudcheflabs.dataroaster.trino.gateway.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TrinoActiveQueryCountRestServiceImpl implements TrinoActiveQueryCountRestService, InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(TrinoActiveQueryCountRestServiceImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    private SimpleHttpClient simpleHttpClient;

    private String restUri;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        restUri = env.getProperty("trino.operator.url");
        LOG.info("trino operator rest uri: {}", restUri);
    }


    @Override
    public TrinoActiveQueryCount getTrinoActiveQueryCount(String clusterName) {
        String urlPath = restUri + "/v1/jmx/get_value";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("namespace", "trino-operator");
        urlBuilder.addQueryParameter("cluster_name", clusterName);
        urlBuilder.addQueryParameter("object_name", "trino.execution:name=QueryManager");
        urlBuilder.addQueryParameter("attribute", "RunningQueries");

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        RestResponse restResponse = ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        if(restResponse.getStatusCode() == RestResponse.STATUS_OK) {
            String json = restResponse.getSuccessMessage();
            List<Map<String, Object>> list = JsonUtils.toMapList(mapper, json);
            for(Map<String, Object> map : list) {
                if(map.get("role").equals("coordinator")) {
                    String address = (String) map.get("address");
                    String count = (String) map.get("value");
                    if(count == null) {
                        count = "0";
                    }
                    LOG.info("cluster name: {}, active query count: {}", clusterName, count);
                    TrinoActiveQueryCount trinoActiveQueryCount = new TrinoActiveQueryCount();
                    trinoActiveQueryCount.setClusterName(clusterName);
                    trinoActiveQueryCount.setCoordinatorAddress(address);
                    trinoActiveQueryCount.setCount(Integer.valueOf(count));
                    return trinoActiveQueryCount;
                }
            }
        } else {
            LOG.error("error: {}", restResponse.getErrorMessage());
            return null;
        }

        return null;
    }
}
