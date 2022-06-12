package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterGroupDao;
import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class ClusterGroupControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupControllerTestRunner.class);

    @Autowired
    private Environment env;

    @Autowired
    private ClusterGroupDao dao;

    private OkHttpClient client;
    private MediaType mediaType;
    private String serverUrl;



    @Before
    public void setup() throws Exception {
        client = new SimpleHttpClient().getClient();
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
        int port = Integer.valueOf(env.getProperty("server.port"));
        serverUrl = "http://localhost:" + port;
    }

    @Test
    public void create() throws Exception {
        doCreate();
    }

    private void doCreate() throws Exception {
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

        ClusterGroup clusterGroup = dao.findOne(groupName);
        Assert.assertNotNull(clusterGroup);
        Assert.assertEquals(groupName, clusterGroup.getGroupName());
    }

    @Test
    public void delete() throws Exception {
        doDelete();
    }

    private void doDelete() throws Exception {
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

        ClusterGroup clusterGroup = dao.findOne(groupName);
        Assert.assertNull(clusterGroup);
    }

    @Test
    public void list() throws Exception {
        doCreate();

        doList();

        doDelete();
    }

    private void doList() throws Exception {
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
