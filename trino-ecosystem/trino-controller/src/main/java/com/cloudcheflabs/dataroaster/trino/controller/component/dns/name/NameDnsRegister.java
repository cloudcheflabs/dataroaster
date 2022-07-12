package com.cloudcheflabs.dataroaster.trino.controller.component.dns.name;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface NameDnsRegister  {
    RestResponse createDnsRecord(String authToken, String domain, String host, String ip);
    RestResponse listDnsRecords(String authToken, String domain);
    RestResponse updateDnsRecord(String authToken, long id, String domain, String host, String ip);
    RestResponse deleteDnsRecord(String authToken, String domain, long id);
}
