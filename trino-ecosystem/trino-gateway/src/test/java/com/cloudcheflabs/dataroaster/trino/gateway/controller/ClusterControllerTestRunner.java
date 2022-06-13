package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterDao;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterControllerTestRunner extends SpringBootTestRunnerBase {

    private static Logger LOG = LoggerFactory.getLogger(ClusterControllerTestRunner.class);

    private static ClusterDao dao;

    @BeforeClass
    public static void setup() throws Exception {
        dao = applicationContext.getBean(ClusterDao.class);
    }
}
