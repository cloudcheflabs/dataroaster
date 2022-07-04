package com.cloudcheflabs.dataroaster.trino.monitor.prometheus;

import com.j256.simplejmx.client.JmxClient;
import org.junit.Test;

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
            System.out.printf("domain: %s, canonicalName: %s, canonicalKeyPropertyListString: %s\n", domain, canonicalName, canonicalKeyPropertyListString);
        }
    }
}
