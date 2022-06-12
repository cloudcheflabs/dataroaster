package com.cloudcheflabs.dataroaster.trino.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrinoGatewayApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TrinoProxyTestRunner {

    @Test
    public void runTrinoProxy() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}
