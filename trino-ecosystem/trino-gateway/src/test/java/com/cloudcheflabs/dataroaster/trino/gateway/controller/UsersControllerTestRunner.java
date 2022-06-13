package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import okhttp3.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsersControllerTestRunner extends ClusterGroupControllerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(UsersControllerTestRunner.class);

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

        String urlPath = serverUrl + "/v1/users/create";

        String user = "trino";
        String password = "trino123";
        String groupName = "etl";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("user", user)
                .add("password", password)
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

        Users users = usersDao.findOne(user);
        Assert.assertEquals(groupName, users.getClusterGroup().getGroupName());
    }

    @Test
    public void list() throws Exception {
        doCreate();

        String urlPath = serverUrl + "/v1/users/list";

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
    public void updatePassword() throws Exception{
        doCreate();

        String urlPath = serverUrl + "/v1/users/update/password";

        String user = "trino";
        String password = "trino123Updated";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("user", user)
                .add("password", password)
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

        Users users = usersDao.findOne(user);
        Assert.assertTrue(BCryptUtils.isMatched(password, users.getPassword()));
    }

    @Test
    public void delete() throws Exception{
        create();

        doDeleteUsers();

        this.doDeleteClusterGroup();
    }

    public void doDeleteUsers() throws Exception {
        String urlPath = serverUrl + "/v1/users/delete";

        String user = "trino";

        // parameters in body.
        RequestBody body = new FormBody.Builder()
                .add("user", user)
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

        Users users = usersDao.findOne(user);
        Assert.assertNull(users);
    }
}
