package com.cloudcheflabs.dataroaster.operators.trino.component;


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
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS));

        return builder.build();
    }

}
