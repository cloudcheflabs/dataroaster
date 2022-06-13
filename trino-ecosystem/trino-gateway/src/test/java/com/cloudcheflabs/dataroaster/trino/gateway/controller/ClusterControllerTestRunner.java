package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterControllerTestRunner extends ClusterGroupControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(ClusterControllerTestRunner.class);

    @BeforeClass
    public static void setup() {
        init();
    }

    @Test
    public void create() throws Exception {
        doCreate();
    }

    public void doCreate() throws Exception {
        // create cluster group.
        this.doCreateClusterGroup();

        String urlPath = serverUrl + "/v1/cluster/create";

        String clusterName = "trino-etl-1";
        String clusterType = "etl";
        String url = "http://localhost:8080";
        String activated = "true";
        String groupName = "etl";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("cluster_name", clusterName)
                .add("cluster_type", clusterType)
                .add("url", url)
                .add("activated", activated)
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

        Cluster cluster = clusterDao.findOne(clusterName);
        Assert.assertEquals(url, cluster.getUrl());
    }

    @Test
    public void list() throws Exception {
        doCreate();

        String urlPath = serverUrl + "/v1/cluster/list";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();

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

    @Test
    public void updateActivated() throws Exception{
        doCreate();

        String urlPath = serverUrl + "/v1/cluster/update/activated";

        String clusterName = "trino-etl-1";
        String activated = "false";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("cluster_name", clusterName)
                .add("activated", activated)
                .build();
        Request request = new Request.Builder()
                .url(urlPath)
                .addHeader("Content-Length", String.valueOf(body.contentLength()))
                .put(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        Assert.assertTrue(response.isSuccessful());
        String ret = responseBody.string();
        LOG.info("ret: [{}]", ret);

        Cluster cluster = clusterDao.findOne(clusterName);
        Assert.assertTrue(!cluster.isActivated());
    }

    @Test
    public void delete() throws Exception{
        create();

        doDeleteCluster();

        this.doDeleteClusterGroup();
    }

    public void doDeleteCluster() throws Exception {
        String urlPath = serverUrl + "/v1/cluster/delete";

        String clusterName = "trino-etl-1";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("cluster_name", clusterName)
                .build();
        Request request = new Request.Builder()
                .url(urlPath)
                .addHeader("Content-Length", String.valueOf(body.contentLength()))
                .delete(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        Assert.assertTrue(response.isSuccessful());
        String ret = responseBody.string();
        LOG.info("ret: [{}]", ret);

        Cluster cluste = clusterDao.findOne(clusterName);
        Assert.assertNull(cluste);
    }
}
