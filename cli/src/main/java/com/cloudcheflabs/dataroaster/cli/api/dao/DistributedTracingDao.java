package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface DistributedTracingDao {
    RestResponse createDistributedTracing(ConfigProps configProps,
                                        long projectId,
                                        long serviceDefId,
                                        long clusterId,
                                        String storageClass,
                                        String ingressHost,
                                        String elasticsearchHostPort);
    RestResponse deleteDistributedTracing(ConfigProps configProps, long serviceId);
}
