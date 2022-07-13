package com.cloudcheflabs.dataroaster.trino.controller.service;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.ClusterJmxDao;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.ClusterJmxService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ClusterJmxServiceImpl implements ClusterJmxService {

    @Autowired
    @Qualifier("restClusterJmxDao")
    private ClusterJmxDao clusterJmxDao;

    @Override
    public RestResponse listClusterJmxEndpoints(String namespace, String restUri) {
        return clusterJmxDao.listClusterJmxEndpoints(namespace, restUri);
    }
}
