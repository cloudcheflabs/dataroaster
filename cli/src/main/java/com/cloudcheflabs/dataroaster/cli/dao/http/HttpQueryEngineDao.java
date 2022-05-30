package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.QueryEngineDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpQueryEngineDao extends AbstractHttpClient implements QueryEngineDao {

    public HttpQueryEngineDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createQueryEngine(ConfigProps configProps,
                                          long projectId,
                                          long serviceDefId,
                                          long clusterId,
                                          String s3Bucket,
                                          String s3AccessKey,
                                          String s3SecretKey,
                                          String s3Endpoint,
                                          String sparkThriftServerStorageClass,
                                          int sparkThriftServerExecutors,
                                          int sparkThriftServerExecutorMemory,
                                          int sparkThriftServerExecutorCores,
                                          int sparkThriftServerDriverMemory,
                                          int trinoWorkers,
                                          int trinoServerMaxMemory,
                                          int trinoCores,
                                          int trinoTempDataStorage,
                                          int trinoDataStorage,
                                          String trinoStorageClass) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/query_engine/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
                .add("s3_bucket", String.valueOf(s3Bucket))
                .add("s3_access_key", String.valueOf(s3AccessKey))
                .add("s3_secret_key", String.valueOf(s3SecretKey))
                .add("s3_endpoint", String.valueOf(s3Endpoint))
                .add("spark_thrift_server_storage_class", String.valueOf(sparkThriftServerStorageClass))
                .add("spark_thrift_server_executors", String.valueOf(sparkThriftServerExecutors))
                .add("spark_thrift_server_executor_memory", String.valueOf(sparkThriftServerExecutorMemory))
                .add("spark_thrift_server_executor_cores", String.valueOf(sparkThriftServerExecutorCores))
                .add("spark_thrift_server_driver_memory", String.valueOf(sparkThriftServerDriverMemory))
                .add("trino_workers", String.valueOf(trinoWorkers))
                .add("trino_server_max_memory", String.valueOf(trinoServerMaxMemory))
                .add("trino_cores", String.valueOf(trinoCores))
                .add("trino_temp_data_storage", String.valueOf(trinoTempDataStorage))
                .add("trino_data_storage", String.valueOf(trinoDataStorage))
                .add("trino_storage_class", String.valueOf(trinoStorageClass))
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
    public RestResponse deleteQueryEngine(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/query_engine/delete";

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
