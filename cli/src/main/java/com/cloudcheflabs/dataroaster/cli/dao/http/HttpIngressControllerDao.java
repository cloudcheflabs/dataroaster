package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.IngressControllerDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.*;

import java.io.IOException;

public class HttpIngressControllerDao extends AbstractHttpClient implements IngressControllerDao {

    public HttpIngressControllerDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createIngressController(ConfigProps configProps, long projectId, long serviceDefId, long clusterId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ingress_controller/create";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("project_id", String.valueOf(projectId))
                .add("service_def_id", String.valueOf(serviceDefId))
                .add("cluster_id", String.valueOf(clusterId))
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
    public RestResponse deleteIngressController(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ingress_controller/delete";

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

    @Override
    public RestResponse getExternalIpOfIngressControllerNginx(ConfigProps configProps, long clusterId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/resource_control/ingress_controller/get_external_ip";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("cluster_id", String.valueOf(clusterId));

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        return ResponseHandler.doCall(client, request);
    }
}
