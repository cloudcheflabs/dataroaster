package com.cloudcheflabs.dataroaster.apiserver.dao.http;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.AuthorizerDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class HttpAuthorizerDao implements AuthorizerDao, InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(HttpAuthorizerDao.class);

    private ObjectMapper mapper = new ObjectMapper();

    private OkHttpClient client;

    private MediaType mediaType;

    @Value("${client.secret}")
    private String clientSecret;

    @Value("${authorizer.url}")
    private String authorizerBaseUrl;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = new SimpleHttpClient().getClient();
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
    }

    @Override
    public AuthorizerResponse login(String userName, String password) {

        AuthorizerResponse authorizerResponse = new AuthorizerResponse();

        String urlPath = authorizerBaseUrl + "/authorizer/oauth/token";

        // parameters in body.
        String content = "grant_type=password&username=" + userName + "&password=" + password;

        RequestBody body = RequestBody.create(mediaType, content);

        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Basic " + clientSecret)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();

            String ret = responseBody.string();
            int statusCode = response.code();

            authorizerResponse.setStatusCode(statusCode);

            if (response.isSuccessful()) {
                authorizerResponse.setSuccessMessage(ret);
            } else {
                authorizerResponse.setErrorMessage(ret);
                responseBody.close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());

            if (response != null) {
                response.close();
            }

            // remove all connections.
            client.connectionPool().evictAll();

            // build new client.
            client = new SimpleHttpClient().getClient();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return authorizerResponse;
    }

    @Override
    public AuthorizerResponse getPrivileges(String accessToken) {

        AuthorizerResponse authorizerResponse = new AuthorizerResponse();

        String urlPath = authorizerBaseUrl + "/authorizer/api/get_privileges?service_type=api";

        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();

            String ret = responseBody.string();
            LOG.debug("responseBody: {}", ret);

            int statusCode = response.code();
            LOG.debug("statusCode: {}", statusCode);

            authorizerResponse.setStatusCode(statusCode);

            if (response.isSuccessful()) {
                authorizerResponse.setSuccessMessage(ret);
            } else {
                authorizerResponse.setErrorMessage(ret);
                responseBody.close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());

            if (response != null) {
                response.close();
            }

            // remove all connections.
            client.connectionPool().evictAll();

            // build new client.
            client = new SimpleHttpClient().getClient();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return authorizerResponse;
    }

    @Override
    public AuthorizerResponse removeToken(String accessToken, String refreshToken) {
        AuthorizerResponse authorizerResponse = new AuthorizerResponse();

        String urlPath = authorizerBaseUrl + "/authorizer/remove_token";

        Response response = null;
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("refresh_token", refreshToken)
                    .build();

            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(formBody)
                    .build();

            response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();

            String ret = responseBody.string();
            int statusCode = response.code();

            authorizerResponse.setStatusCode(statusCode);

            if (response.isSuccessful()) {
                authorizerResponse.setSuccessMessage(ret);
            } else {
                authorizerResponse.setErrorMessage(ret);
                responseBody.close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());

            if (response != null) {
                response.close();
            }

            // remove all connections.
            client.connectionPool().evictAll();

            // build new client.
            client = new SimpleHttpClient().getClient();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return authorizerResponse;
    }
}
