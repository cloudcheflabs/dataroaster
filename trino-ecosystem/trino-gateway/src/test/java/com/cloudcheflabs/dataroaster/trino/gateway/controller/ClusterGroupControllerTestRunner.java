package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterGroupControllerTestRunner extends SpringBootTestRunnerBase {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupControllerTestRunner.class);

    @Before
    public static void setup() {
        init();
    }
    @Test
    public void create() throws Exception {
        doCreateClusterGroup();
    }

    public void doCreateClusterGroup() throws Exception {
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

        ClusterGroup clusterGroup = clusterGroupDao.findOne(groupName);
        Assert.assertNotNull(clusterGroup);
        Assert.assertEquals(groupName, clusterGroup.getGroupName());
    }

    @Test
    public void delete() throws Exception {
        doDeleteClusterGroup();
    }

    public void doDeleteClusterGroup() throws Exception {
        String urlPath = serverUrl + "/v1/cluster_group/delete";

        String groupName = "etl";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("group_name", groupName)
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

        ClusterGroup clusterGroup = clusterGroupDao.findOne(groupName);
        Assert.assertNull(clusterGroup);
    }

    @Test
    public void list() throws Exception {
        doCreateClusterGroup();

        doListClusterGroup();

        doDeleteClusterGroup();
    }

    public void doListClusterGroup() throws Exception {
        String urlPath = serverUrl + "/v1/cluster_group/list";

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
}
