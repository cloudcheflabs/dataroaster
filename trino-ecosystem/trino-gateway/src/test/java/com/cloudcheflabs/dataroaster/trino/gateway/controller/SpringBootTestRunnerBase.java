package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.TrinoGatewayApplication;
import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;

public class SpringBootTestRunnerBase {
    private static Logger LOG = LoggerFactory.getLogger(SpringBootTestRunnerBase.class);

    protected static ConfigurableApplicationContext applicationContext;

    protected static Environment env;

    protected static OkHttpClient client;
    protected static MediaType mediaType;

    protected static String serverUrl;

    protected static void init() {
        // run spring boot application.
        applicationContext =
                SpringApplication.run(TrinoGatewayApplication.class, Arrays.asList("").toArray(new String[0]));
        env = applicationContext.getBean(Environment.class);

        client = new SimpleHttpClient().getClient();
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String port = env.getProperty("server.port");
        serverUrl = "http://localhost:" + port;
        LOG.info("serverUrl: [{}]", serverUrl);
    }
}
