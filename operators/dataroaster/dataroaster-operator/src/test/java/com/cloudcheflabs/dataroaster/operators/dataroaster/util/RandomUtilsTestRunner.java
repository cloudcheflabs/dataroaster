package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomUtilsTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(RandomUtilsTestRunner.class);

    @Test
    public void random() throws Exception {

        LOG.info("random base64 encoded: [{}]", RandomUtils.randomPassword());
    }
}
