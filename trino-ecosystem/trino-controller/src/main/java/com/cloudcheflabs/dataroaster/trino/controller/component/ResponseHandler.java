package com.cloudcheflabs.dataroaster.trino.controller.component;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ResponseHandler {

    public static RestResponse doCall(OkHttpClient client, Request request) {
        RestResponse restResponse = new RestResponse();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();

            String ret = responseBody.string();
            int statusCode = response.code();

            restResponse.setStatusCode(statusCode);

            if (response.isSuccessful()) {
                restResponse.setSuccessMessage(ret);
            } else {
                restResponse.setErrorMessage(ret);
                responseBody.close();
            }
        } catch (Exception e) {
            restResponse.setErrorMessage(e.getMessage());

            if (response != null) {
                response.close();
            }

            // remove all connections.
            client.connectionPool().evictAll();

            // build new client.
            client = new SimpleHttpClient().getClient();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return restResponse;
    }
}
