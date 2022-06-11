package com.cloudcheflabs.dataroaster.trino.gateway.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class SpringTestBase {
}
