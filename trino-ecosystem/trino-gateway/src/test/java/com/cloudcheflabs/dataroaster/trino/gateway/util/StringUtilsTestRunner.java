package com.cloudcheflabs.dataroaster.trino.gateway.util;

import org.junit.Test;

public class StringUtilsTestRunner {

    @Test
    public void changeHost() throws Exception {
        String nextUri = "http://trino-3.trino-gateway.dataroaster:30080/v1/statement/queued/20220710_030431_00001_kdsmu/y23d143e1a1b19253d7e7390416d36cebabd311ce/1";
        String hostName = "https://trino-gateway-proxy-test.cloudchef-labs.com";


        String[] tokens = nextUri.split("/");

        int count = 0;
        StringBuffer sb = new StringBuffer();
        for(String token : tokens) {
            if(count > 2) {
                sb.append("/").append(token);
            }
            count++;
        }

        System.out.println(sb.toString());

    }
}
