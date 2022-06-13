package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterControllerTestRunner extends ClusterGroupControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(ClusterControllerTestRunner.class);

    @Before
    public static void setup() {
        init();
    }

    @Test
    public void create() throws Exception {
        // create cluster group.
        this.doCreateClusterGroup();

        String urlPath = serverUrl + "/v1/cluster_group/create";

        String groupName = "etl";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("group_name", groupName)
                .build();
        Request request = new Request.Builder()
                .url(urlPath)
                .addHeader("Content-Length", String.valueOf(body.contentLength()))
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        Assert.assertTrue(response.isSuccessful());
        String ret = responseBody.string();
        LOG.info("ret: [{}]", ret);

    }
}
