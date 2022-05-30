package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.AnalyticsDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpAnalyticsDao extends AbstractHttpClient implements AnalyticsDao {

    public HttpAnalyticsDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createAnalytics(ConfigProps configProps,
                                        long projectId,
                                        long serviceDefId,
                                        long clusterId,
                                        String jupyterhubGithubClientId,
                                        String jupyterhubGithubClientSecret,
                                        String jupyterhubIngressHost,
                                        String storageClass,
                                        int jupyterhubStorageSize,
                                        int redashStorageSize) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/analytics/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
                .add("jupyterhub_github_client_id", String.valueOf(jupyterhubGithubClientId))
                .add("jupyterhub_github_client_secret", String.valueOf(jupyterhubGithubClientSecret))
                .add("jupyterhub_ingress_host", String.valueOf(jupyterhubIngressHost))
                .add("storage_class", String.valueOf(storageClass))
                .add("jupyterhub_storage_size", String.valueOf(jupyterhubStorageSize))
                .add("redash_storage_size", String.valueOf(redashStorageSize))
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
    public RestResponse deleteAnalytics(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/analytics/delete";

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
