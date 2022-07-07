package com.cloudcheflabs.dataroaster.operators.trino.jmx;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.operators.trino.util.JmxUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXClientTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(JMXClientTestRunner.class);

    @Test
    public void printAllMBeans() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");

        String mbeanValues = JmxUtils.printAllMBeanValues(host, port);
        LOG.info("mbeanValues: \n{}", mbeanValues);
    }

    @Test
    public void listAllBeanValuesInJson() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");

        String json = JmxUtils.listAllMBeanValuesInJson(host, port);
        LOG.info("mbean values in json: \n{}", JsonWriter.formatJson(json));
    }

    @Test
    public void testGetValue() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");
        String objectName = System.getProperty("objectName");
        String attribute = System.getProperty("attribute");
        String compositeKey = System.getProperty("compositeKey");

        String maxPoolSize = JmxUtils.getValue(host, port,
                "trino.execution:name=QueryExecution",
                "Executor.MaximumPoolSize");
        LOG.info("trino.execution:name=QueryExecution - Executor.MaximumPoolSize: {}", maxPoolSize);

        String heapMemory = JmxUtils.getValue(host, port,
                "java.lang:type=Memory",
                "HeapMemoryUsage",
                "committed");
        LOG.info("java.lang:type=Memory - HeapMemoryUsage - committed: {}", heapMemory);
    }

    @Test
    public void getValue() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");
        String objectName = System.getProperty("objectName");
        String attribute = System.getProperty("attribute");
        String compositeKey = System.getProperty("compositeKey");

        String value = JmxUtils.getValue(host, port,
                objectName,
                attribute,
                compositeKey);
        LOG.info("{} - {} - {}: {}", objectName, attribute, compositeKey, value);
    }
}
