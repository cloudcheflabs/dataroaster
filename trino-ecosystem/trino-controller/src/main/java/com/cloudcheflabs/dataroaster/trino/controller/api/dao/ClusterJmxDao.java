package com.cloudcheflabs.dataroaster.trino.controller.api.dao;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface ClusterJmxDao {
    RestResponse listClusterJmxEndpoints(String namespace, String restUri);
}
