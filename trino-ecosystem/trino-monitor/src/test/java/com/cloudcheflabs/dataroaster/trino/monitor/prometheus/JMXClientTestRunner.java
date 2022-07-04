package com.cloudcheflabs.dataroaster.trino.monitor.prometheus;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.j256.simplejmx.client.JmxClient;
import com.j256.simplejmx.common.ObjectNameUtil;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
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
                        // in case of composite data.
                        if(attributeValue instanceof CompositeData) {
                            CompositeData compositeData = (CompositeData) attributeValue;
                            System.out.printf("\t\t%s\n", attribute);
                            for(String compositeKey : compositeData.getCompositeType().keySet()) {
                                System.out.printf("\t\t\t%s=%s\n", compositeKey, compositeData.get(compositeKey));
                            }
                        } else {
                            System.out.printf("\t\t%s=%s\n", attribute, attributeValue);
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
                }
            }
        }
    }
}
