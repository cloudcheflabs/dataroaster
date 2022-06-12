package com.cloudcheflabs.dataroaster.trino.gateway.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCryptTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(BCryptTestRunner.class);

    @Test
    public void bcrypt() throws Exception {
        String password = "mypass123";

        String bcryptEncodedPassword = BCryptUtils.encodeWithBCrypt(password);
        Assert.assertTrue(BCryptUtils.isMatched(password, bcryptEncodedPassword));
        LOG.info("bcryptEncodedPassword: [{}]", bcryptEncodedPassword);

        Assert.assertTrue(!BCryptUtils.isMatched(password + "4", bcryptEncodedPassword));
    }
}
