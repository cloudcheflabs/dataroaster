package com.cloudcheflabs.dataroaster.trino.gateway.api.service;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;

public interface TrinoActiveQueryCountRestService {

    TrinoActiveQueryCount getTrinoActiveQueryCount(String clusterName);
}
