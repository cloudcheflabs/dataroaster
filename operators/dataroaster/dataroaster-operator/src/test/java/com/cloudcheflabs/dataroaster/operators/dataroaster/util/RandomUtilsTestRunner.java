package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomUtilsTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(RandomUtilsTestRunner.class);

    @Test
    public void random() throws Exception {

        String random = RandomUtils.randomPassword();
        LOG.info("random: [{}]", random);

        LOG.info("bcrypted: [{}]", BCryptUtils.encodeWithBCrypt(random));
    }
}
