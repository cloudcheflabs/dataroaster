package com.cloudcheflabs.dataroaster.trino.controller.component.dns.name;

import com.cloudcheflabs.dataroaster.trino.controller.component.ResponseHandler;
import com.cloudcheflabs.dataroaster.trino.controller.component.SimpleHttpClient;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NameDnsRegisterImpl implements NameDnsRegister {


    private static final String NAME_URL = "https://api.name.com";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    private SimpleHttpClient simpleHttpClient;

    private String makeData(String host, String ip) {
        return "{\"host\":\"" + host + "\",\"type\":\"A\",\"answer\":\"" + ip + "\",\"ttl\":300}";
    }


    @Override
    public RestResponse createDnsRecord(String authToken, String domain, String host, String ip) {
        String urlPath = NAME_URL + "/v4/domains/" + domain + "/records";

        // body.
        RequestBody body = RequestBody.create(JSON, makeData(host, ip));
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Basic " + authToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestResponse listDnsRecords(String authToken, String domain) {
        String urlPath = NAME_URL + "/v4/domains/" + domain + "/records";

        // parameters in body.
        String content = "";

        RequestBody body = RequestBody.create(JSON, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Basic " + authToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .get()
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestResponse updateDnsRecord(String authToken, long id, String domain, String host, String ip) {
        String urlPath = NAME_URL + "/v4/domains/" + domain + "/records" + "/" + id;

        String content = "";

        RequestBody body = RequestBody.create(JSON, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Basic " + authToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .put(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestResponse deleteDnsRecord(String authToken, String domain, long id) {
        String urlPath = NAME_URL + "/v4/domains/" + domain + "/records" + "/" + id;

        String content = "";

        RequestBody body = RequestBody.create(JSON, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Basic " + authToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(simpleHttpClient.getClient(), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
