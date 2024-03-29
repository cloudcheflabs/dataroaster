package com.cloudcheflabs.dataroaster.trino.gateway.domain;

import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseHandler {

    private static Logger LOG = LoggerFactory.getLogger(ResponseHandler.class);

    public static RestResponse doCall(OkHttpClient client, Request request) {
        RestResponse restResponse = new RestResponse();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();

            String ret = responseBody.string();
            //LOG.info("ret: {}", ret);
            int statusCode = response.code();

            restResponse.setStatusCode(statusCode);

            if (response.isSuccessful()) {
                restResponse.setSuccessMessage(ret);
            } else {
                restResponse.setErrorMessage(ret);
                responseBody.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            restResponse.setErrorMessage(e.getMessage());

            if (response != null) {
                response.close();
            }

            // remove all connections.
            client.connectionPool().evictAll();

            // build new client.
            client = new SimpleHttpClient().getClient();
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return restResponse;
    }
}
