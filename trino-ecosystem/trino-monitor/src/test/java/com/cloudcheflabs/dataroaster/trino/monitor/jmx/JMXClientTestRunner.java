package com.cloudcheflabs.dataroaster.trino.monitor.jmx;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.monitor.util.JmxUtils;
import com.j256.simplejmx.client.JmxClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Hashtable;
import java.util.Set;

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

        String json = JmxUtils.listAllMBeanValues(host, port);
        LOG.info("mbean values in json: \n{}", JsonWriter.formatJson(JmxUtils.listAllMBeanValues(host, port)));
    }

    @Test
    public void getValue() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");
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
}
