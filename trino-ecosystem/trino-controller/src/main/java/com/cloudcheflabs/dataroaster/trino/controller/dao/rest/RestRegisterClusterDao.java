package com.cloudcheflabs.dataroaster.trino.controller.dao.rest;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.RegisterClusterDao;
import com.cloudcheflabs.dataroaster.trino.controller.dao.common.AbstractRestDao;
import com.cloudcheflabs.dataroaster.trino.controller.domain.ResponseHandler;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Repository;

@Repository
public class RestRegisterClusterDao extends AbstractRestDao implements RegisterClusterDao {

    @Override
    public RestResponse createClusterGroup(String restUri, String groupName) {
        String urlPath = restUri + "/v1/cluster_group/create";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("group_name", groupName)
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse listClusterGroup(String restUri) {
        String urlPath = restUri + "/v1/cluster_group/list";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }

    @Override
    public RestResponse deleteClusterGroup(String restUri, String groupName) {
        String urlPath = restUri + "/v1/cluster_group/delete";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("group_name", groupName)
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse registerCluster(String restUri, String clusterName, String clusterType, String url, boolean activated, String groupName) {
        String urlPath = restUri + "/v1/cluster/create";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("cluster_name", clusterName)
                    .add("cluster_type", clusterType)
                    .add("url", url)
                    .add("activated", String.valueOf(activated))
                    .add("group_name", groupName)
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse updateClusterActivated(String restUri, String clusterName, boolean activated) {
        String urlPath = restUri + "/v1/cluster/update/activated";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("cluster_name", clusterName)
                    .add("activated", String.valueOf(activated))
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .put(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse listClusters(String restUri) {
        String urlPath = restUri + "/v1/cluster/list";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }

    @Override
    public RestResponse deregisterCluster(String restUri, String clusterName) {
        String urlPath = restUri + "/v1/cluster/delete";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("cluster_name", clusterName)
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse createUser(String restUri, String user, String password, String groupName) {
        String urlPath = restUri + "/v1/users/create";

        try {
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

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse updatePassword(String restUri, String user, String password) {
        String urlPath = restUri + "/v1/users/update/password";

        try {
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

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RestResponse listUsers(String restUri) {
        String urlPath = restUri + "/v1/users/list";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlPath).newBuilder();

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
    }

    @Override
    public RestResponse deleteUser(String restUri, String user) {
        String urlPath = restUri + "/v1/users/delete";

        try {
            // parameters in body.
            RequestBody body = new FormBody.Builder()
                    .add("user", user)
                    .build();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
