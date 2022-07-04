package com.cloudcheflabs.dataroaster.trino.monitor.prometheus;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.j256.simplejmx.client.JmxClient;
import com.j256.simplejmx.common.ObjectNameUtil;
import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import java.util.Set;

public class JMXClientTestRunner {

    @Test
    public void getMetrics() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "9080");

        JmxClient client = new JmxClient(host, Integer.valueOf(port));
        Set<ObjectName> names = client.getBeanNames();
        for (ObjectName name : names) {
            String domain = name.getDomain();
            String canonicalName = name.getCanonicalName();
            String canonicalKeyPropertyListString = name.getCanonicalKeyPropertyListString();
            String keyPropertyListString = name.getKeyPropertyListString();
            System.out.printf("domain: %s, canonicalName: %s, canonicalKeyPropertyListString: %s, keyPropertyListString: %s\n", domain, canonicalName, canonicalKeyPropertyListString, keyPropertyListString);
        }

        ObjectName objectName = ObjectNameUtil.makeObjectName("trino.execution", "QueryExecution");
        MBeanAttributeInfo[] infos = client.getAttributesInfo(objectName);
        for(MBeanAttributeInfo info : infos) {
            String attribute = info.getName();
            Object obj = client.getAttribute(objectName, attribute);
            System.out.printf("attribute: %s, obj: %s\n", attribute, JsonUtils.toJson(obj));
        }
    }
}
