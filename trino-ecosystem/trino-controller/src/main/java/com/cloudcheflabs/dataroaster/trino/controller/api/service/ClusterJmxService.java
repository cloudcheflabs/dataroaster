package com.cloudcheflabs.dataroaster.trino.controller.api.service;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface ClusterJmxService {
    RestResponse listClusterJmxEndpoints(String namespace, String restUri);
}
