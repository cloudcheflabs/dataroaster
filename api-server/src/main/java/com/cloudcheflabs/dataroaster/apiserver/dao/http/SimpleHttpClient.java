package com.cloudcheflabs.dataroaster.apiserver.dao.http;


import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class SimpleHttpClient {

    private OkHttpClient client;


    public SimpleHttpClient()
    {
        this.client = buildClient();
    }

    public OkHttpClient getClient()
    {
        return this.client;
    }


    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS));

        return builder.build();
    }

}
