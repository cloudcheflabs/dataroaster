package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface MetricsMonitoringDao {
    RestResponse createMetricsMonitoring(ConfigProps configProps,
                                        long projectId,
                                        long serviceDefId,
                                        long clusterId,
                                        String storageClass,
                                        int storageSize);
    RestResponse deleteMetricsMonitoring(ConfigProps configProps, long serviceId);
}
