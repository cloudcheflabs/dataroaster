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

        System.out.println(hostName + sb.toString());

    }

    @Test
    public void getQueryId() throws Exception {
        String uri = "/v1/statement/executing/20220710_072650_00018_532z5/y1edf3c39ee4dffe419f0c4cb2b9b86544b8e24d8/0";

        uri = uri.replaceAll("/v1/statement/executing/", "");

        String[] tokens = uri.split("/");
        System.out.println("query id: [" + tokens[0] + "]");
    }
}
