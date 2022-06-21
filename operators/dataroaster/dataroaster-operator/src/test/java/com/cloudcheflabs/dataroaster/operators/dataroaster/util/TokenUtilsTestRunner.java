package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class TokenUtilsTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(TokenUtilsTestRunner.class);

    @Test
    public void createNewToken() throws Exception {
        String generatedString = RandomUtils.randomText();
        LOG.info("generatedString: [{}]", generatedString);

        String bcrypted = BCryptUtils.encodeWithBCrypt(generatedString);
        LOG.info("bcrypted: [{}]", bcrypted);

        String encodedString = Base64.getEncoder().encodeToString(bcrypted.getBytes());
        LOG.info("encodedString: [{}]", encodedString);
    }
}
