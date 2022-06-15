package com.cloudcheflabs.dataroaster.trino.gateway;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

public class TrinoProxyTestRunner {

    @BeforeClass
    public static void setup() throws Exception {
        // run spring boot application.
        ConfigurableApplicationContext applicationContext =
                SpringApplication.run(TrinoGatewayApplication.class, Arrays.asList("").toArray(new String[0]));
    }


    @Test
    public void run() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}
