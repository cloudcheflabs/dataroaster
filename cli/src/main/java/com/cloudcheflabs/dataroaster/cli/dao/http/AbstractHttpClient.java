package com.cloudcheflabs.dataroaster.cli.dao.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public abstract class AbstractHttpClient {

    protected OkHttpClient client;

    protected MediaType mediaType;

    public AbstractHttpClient(OkHttpClient client) {
        this.client = client;
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
    }

}
