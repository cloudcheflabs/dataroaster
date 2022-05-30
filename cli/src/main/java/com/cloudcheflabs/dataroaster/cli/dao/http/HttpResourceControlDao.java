package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpResourceControlDao extends AbstractHttpClient implements ResourceControlDao {

    public HttpResourceControlDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse listStorageClasses(ConfigProps configProps, long clusterId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/resource_control/list_storageclass";

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
