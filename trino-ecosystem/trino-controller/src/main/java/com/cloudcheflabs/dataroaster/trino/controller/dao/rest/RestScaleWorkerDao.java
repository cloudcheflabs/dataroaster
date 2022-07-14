package com.cloudcheflabs.dataroaster.trino.controller.dao.rest;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.ScaleWorkerDao;
import com.cloudcheflabs.dataroaster.trino.controller.dao.common.AbstractRestDao;
import com.cloudcheflabs.dataroaster.trino.controller.domain.ResponseHandler;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Repository;

@Repository
public class RestScaleWorkerDao extends AbstractRestDao implements ScaleWorkerDao {


    @Override
    public RestResponse listWorkerCount(String restUri, String namespace) {
        String urlPath = restUri + "/v1/scale/list_worker_count";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("namespace", namespace);

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }

    @Override
    public RestResponse scaleOutWorkers(String restUri, String namespace, String name, int replicas) {
        String urlPath = restUri + "/v1/scale/scale_workers";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("namespace", namespace)
                    .add("cluster_name", name)
                    .add("replicas", String.valueOf(replicas))
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .put(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse listHpa(String restUri, String namespace) {
        String urlPath = restUri + "/v1/scale/list_hpa";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("namespace", namespace);

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }

    @Override
    public RestResponse updateHpa(String restUri, String namespace, String name, int minReplicas, int maxReplicas) {
        String urlPath = restUri + "/v1/scale/scale_hpa";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("namespace", namespace)
                    .add("cluster_name", name)
                    .add("min_replicas", String.valueOf(minReplicas))
                    .add("max_replicas", String.valueOf(maxReplicas))
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .put(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
