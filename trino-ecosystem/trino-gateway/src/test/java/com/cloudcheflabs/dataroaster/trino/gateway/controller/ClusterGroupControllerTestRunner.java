package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.TrinoGatewayApplication;
import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterGroupDao;
import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import okhttp3.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.Properties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrinoGatewayApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
public class ClusterGroupControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupControllerTestRunner.class);


    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClusterGroupDao dao;

    private OkHttpClient client;
    private MediaType mediaType;

    private String serverUrl;


    @BeforeEach
    public void setup() throws Exception {
        client = new SimpleHttpClient().getClient();
        mediaType = MediaType.parse("application/x-www-form-urlencoded");

        InputStream is = FileUtils.readFileFromClasspath("application-test.properties");
        Properties prop = new Properties();
        prop.load(is);
        String port = prop.getProperty("server.port");
        serverUrl = "http://localhost:" + port;
        LOG.info("serverUrl: [{}]", serverUrl);
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
