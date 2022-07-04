package com.cloudcheflabs.dataroaster.trino.monitor.prometheus;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.j256.simplejmx.client.JmxClient;
import com.j256.simplejmx.common.ObjectNameUtil;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import java.util.Hashtable;
import java.util.Set;

public class JMXClientTestRunner {

    @Test
    public void getMetrics() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");

        JmxClient client = new JmxClient(host, Integer.valueOf(port));
        Set<ObjectName> names = client.getBeanNames();
        for (ObjectName name : names) {
            // domain.
            String domain = name.getDomain();
            System.out.println(domain);
            Hashtable<String, String> kv = name.getKeyPropertyList();
            for(String key : kv.keySet()) {
                // key properties.
                String value = kv.get(key);
                System.out.printf("\t%s=%s\n", key, value);
                ObjectName objectName = new ObjectName(domain + ":" + key + "=" + value);
                try {
                    MBeanAttributeInfo[] infos = client.getAttributesInfo(objectName);
                    for (MBeanAttributeInfo info : infos) {
                        // attribute.
                        String attribute = info.getName();
                        Object attributeValue = client.getAttribute(objectName, attribute);
                        System.out.printf("\t\t%s=%s\n", attribute, attributeValue);
                    }
                } catch (JMException e) {
                    //System.err.println(e.getMessage());
                }
            }
        }
    }
}
