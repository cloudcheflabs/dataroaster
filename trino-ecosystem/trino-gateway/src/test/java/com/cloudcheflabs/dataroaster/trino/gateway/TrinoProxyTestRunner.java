package com.cloudcheflabs.dataroaster.trino.gateway;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

public class TrinoProxyTestRunner {

    @BeforeClass
    public static void setup() throws Exception {
        SpringApplication.run(TrinoGatewayApplication.class, Arrays.asList("").toArray(new String[0]));
    }


    @Test
    public void run() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}
