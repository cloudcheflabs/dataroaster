package com.cloudcheflabs.dataroaster.trino.controller.dao.rest;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.ClusterJmxDao;
import com.cloudcheflabs.dataroaster.trino.controller.dao.common.AbstractRestDao;
import com.cloudcheflabs.dataroaster.trino.controller.domain.ResponseHandler;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.springframework.stereotype.Repository;

@Repository
public class RestClusterJmxDao extends AbstractRestDao implements ClusterJmxDao {


    @Override
    public RestResponse listClusterJmxEndpoints(String namespace, String restUri) {
        String urlPath = restUri + "/v1/cluster/list_clusters";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("namespace", namespace);

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }
}
