package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.PrivateRegistryDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpPrivateRegistryDao extends AbstractHttpClient implements PrivateRegistryDao {

    public HttpPrivateRegistryDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createPrivateRegistry(ConfigProps configProps, long projectId, long serviceDefId, long clusterId, String coreHost, String notaryHost, String storageClass, int registryStorageSize, int chartmuseumStorageSize, int jobserviceStorageSize, int databaseStorageSize, int redisStorageSize, int trivyStorageSize, String s3Bucket, String s3AccessKey, String s3SecretKey, String s3Endpoint) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/private_registry/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
                .add("core_host", String.valueOf(coreHost))
                .add("notary_host", String.valueOf(notaryHost))
                .add("storage_class", String.valueOf(storageClass))
                .add("registry_storage_size", String.valueOf(registryStorageSize))
                .add("chartmuseum_storage_size", String.valueOf(chartmuseumStorageSize))
                .add("jobservice_storage_size", String.valueOf(jobserviceStorageSize))
                .add("database_storage_size", String.valueOf(databaseStorageSize))
                .add("redis_storage_size", String.valueOf(redisStorageSize))
                .add("trivy_storage_size", String.valueOf(trivyStorageSize))
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
    public RestResponse deletePrivateRegistry(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/private_registry/delete";

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
