package com.cloudcheflabs.dataroaster.trino.controller.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class PropertiesTestRunner {

    @Test
    public void readMemoryProperties() throws Exception {
        String memoryProperties = FileUtils.fileToString("/memory-properties/memory.properties", true);
        Properties memoryPropObj = new Properties();
        memoryPropObj.load(new ByteArrayInputStream(memoryProperties.getBytes()));

        String configProperties = FileUtils.fileToString("/memory-properties/config.properties", true);
        Properties configPropObj = new Properties();
        configPropObj.load(new ByteArrayInputStream(configProperties.getBytes()));

        for(Object keyObj : memoryPropObj.keySet()) {
            if(configPropObj.containsKey(keyObj)) {
                configPropObj.put(keyObj, memoryPropObj.get(keyObj));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        configPropObj.store(out, null);

        String changedConfigProperties = new String(out.toByteArray());
        System.out.println(changedConfigProperties);
    }
}
