package com.cloudcheflabs.dataroaster.operators.trino.controller;

import com.cloudcheflabs.dataroaster.operators.trino.component.SimpleHttpClient;
import com.cloudcheflabs.dataroaster.operators.trino.handler.CoordinatorWorkerHandlerTestRunner;
import okhttp3.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(JmxControllerTestRunner.class);

    private static OkHttpClient client;
    private static MediaType mediaType;

    @BeforeClass
    public static void setup() throws Exception {
        client = new SimpleHttpClient().getClient();
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
    }

    @Test
    public void getValue() throws Exception {
        String urlPath = "http://localhost:8092/v1/jmx/get_value";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();
        urlBuilder.addQueryParameter("namespace", "trino-operator");
        urlBuilder.addQueryParameter("cluster_name", "trino-cluster-etl");
        urlBuilder.addQueryParameter("object_name", "java.lang:type=Memory");
        urlBuilder.addQueryParameter("attribute", "HeapMemoryUsage");
        urlBuilder.addQueryParameter("composite_key", "committed");

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        Assert.assertTrue(response.isSuccessful());
        String ret = responseBody.string();
        LOG.info("ret: [{}]", ret);
    }
}
