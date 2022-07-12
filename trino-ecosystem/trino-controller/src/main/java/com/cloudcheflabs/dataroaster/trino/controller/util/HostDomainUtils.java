package com.cloudcheflabs.dataroaster.trino.controller.util;

public class HostDomainUtils {

    public static HostDomain getHostDomain(String uri) {
        String[] tokens = uri.split("/");
        for(String token : tokens) {
            if(token.contains(".")) {
                String[] uriTokens = token.split("\\.");
                String domain = uriTokens[uriTokens.length -2] + "." + uriTokens[uriTokens.length -1];

                String hostWithDot = token.replaceAll(domain, "");
                String host = hostWithDot.substring(0, hostWithDot.length() - 1);
                return new HostDomain(host, domain);
            }
        }
        return null;
    }

    public static class HostDomain {
        private String host;
        private String domain;

        public HostDomain(String host, String domain) {
            this.host = host;
            this.domain = domain;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
}
