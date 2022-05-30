package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.KubeconfigDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpKubeconfigDao extends AbstractHttpClient implements KubeconfigDao {

    public HttpKubeconfigDao(OkHttpClient client) {
        super(client);
    }


    @Override
    public RestResponse createKubeconfig(ConfigProps configProps, long id, String kubeconfig) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/k8s/create_kubeconfig";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("cluster_id", String.valueOf(id))
                .add("kubeconfig", String.valueOf(kubeconfig))
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
    public RestResponse updateKubeconfig(ConfigProps configProps, long id, String kubeconfig) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/k8s/update_kubeconfig";

        // parameters in body.
        String content = "cluster_id=" + id + "&kubeconfig=" + kubeconfig;

        RequestBody body = RequestBody.create(mediaType, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .put(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
