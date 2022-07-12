package com.cloudcheflabs.dataroaster.trino.controller.util;

import org.junit.Test;

import java.net.URI;

public class StringUtilTestRunner {

    @Test
    public void getHostAndDomain() throws Exception {
        String publicEndPoint = "https://trino-gateway-proxy-test.cloudchef-labs.com";

        String[] tokens = publicEndPoint.split("/");
        for(String token : tokens) {
            if(token.contains(".")) {
                String[] uriTokens = token.split("\\.");
                String domain = uriTokens[uriTokens.length -2] + "." + uriTokens[uriTokens.length -1];

                String hostWithDot = token.replaceAll(domain, "");
                String host = hostWithDot.substring(0, hostWithDot.length() - 1);
                System.out.printf("host: [%s], domain: [%s]\n", host, domain);
            }
        }
    }
}
