package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.WorkflowDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpWorkflowDao extends AbstractHttpClient implements WorkflowDao {

    public HttpWorkflowDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createWorkflow(ConfigProps configProps,
                                       long projectId,
                                       long serviceDefId,
                                       long clusterId,
                                       String storageClass,
                                       int storageSize,
                                       String s3Bucket,
                                       String s3AccessKey,
                                       String s3SecretKey,
                                       String s3Endpoint) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/workflow/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
                .add("storage_class", String.valueOf(storageClass))
                .add("storage_size", String.valueOf(storageSize))
                .add("s3_bucket", String.valueOf(s3Bucket))
                .add("s3_access_key", String.valueOf(s3AccessKey))
                .add("s3_secret_key", String.valueOf(s3SecretKey))
                .add("s3_endpoint", String.valueOf(s3Endpoint))
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
    public RestResponse deleteWorkflow(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/workflow/delete";

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
