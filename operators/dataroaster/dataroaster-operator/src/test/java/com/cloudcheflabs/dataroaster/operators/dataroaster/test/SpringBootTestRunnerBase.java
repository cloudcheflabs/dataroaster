package com.cloudcheflabs.dataroaster.operators.dataroaster.test;

import com.cloudcheflabs.dataroaster.operators.dataroaster.DataRoasterApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

public class SpringBootTestRunnerBase {

    private static Logger LOG = LoggerFactory.getLogger(SpringBootTestRunnerBase.class);

    protected static ConfigurableApplicationContext applicationContext;

    protected static void init() throws Exception {
        applicationContext =
                SpringApplication.run(DataRoasterApplication.class, Arrays.asList("").toArray(new String[0]));
    }
}
