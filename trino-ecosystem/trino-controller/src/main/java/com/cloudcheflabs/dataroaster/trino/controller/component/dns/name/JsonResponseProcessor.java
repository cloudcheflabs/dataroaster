package com.cloudcheflabs.dataroaster.trino.controller.component.dns.name;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class JsonResponseProcessor {

    private static Logger LOG = LoggerFactory.getLogger(JsonResponseProcessor.class);

    public static final class DnsRecord {
        private long id;
        private String domainName;
        private String host;
        private String fqdn;
        private String type;
        private String answer;
        private int ttl;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getFqdn() {
            return fqdn;
        }

        public void setFqdn(String fqdn) {
            this.fqdn = fqdn;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
    }

    public static DnsRecord getExistingRecord(String json, String host) {
        Map<String, Object> map = JsonUtils.toMap(new ObjectMapper(), json);
        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("records");
        for(Map<String, Object> recordMap : list) {
            String type = (String) recordMap.get("type");
            if(type.equals("A")) {
                String tempHost = (String) recordMap.get("host");
                if (tempHost.equals(host)) {

                    return new ObjectMapper().convertValue(recordMap, DnsRecord.class);
                }
            }
        }
        return null;
    }

}