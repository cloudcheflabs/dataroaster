package com.cloudcheflabs.dataroaster.trino.gateway;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrinoGatewayApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
public class TrinoProxyTestRunner {

    @Autowired
    private MockMvc mvc;

    @Test
    public void run() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}
