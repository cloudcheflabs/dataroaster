package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class IdUtilsTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(IdUtilsTestRunner.class);

    @Test
    public void newId() throws Exception {
        List<String> stringList = Arrays.asList("HelmChart", "mysql", "trino-operator");
        String newId = IdUtils.newId(stringList);
        LOG.info("newId: [{}]", newId);

        Assert.assertTrue(IdUtils.isMatched(stringList, newId));
    }
}
