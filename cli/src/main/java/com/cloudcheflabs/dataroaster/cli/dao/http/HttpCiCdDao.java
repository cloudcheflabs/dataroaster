package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.CiCdDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpCiCdDao extends AbstractHttpClient implements CiCdDao {

    public HttpCiCdDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createCiCd(ConfigProps configProps, long projectId, long serviceDefId, long clusterId, String argocdIngressHost, String jenkinsIngressHost, String storageClass) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ci_cd/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
                .add("argocd_ingress_host", String.valueOf(argocdIngressHost))
                .add("jenkins_ingress_host", String.valueOf(jenkinsIngressHost))
                .add("storage_class", String.valueOf(storageClass))
                .build();
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestResponse deleteCiCd(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ci_cd/delete";

        // parameters in body.
        String content = "service_id=" + serviceId;

        RequestBody body = RequestBody.create(mediaType, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
