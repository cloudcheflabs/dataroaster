package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface PodLogMonitoringDao {
    RestResponse createPodLogMonitoring(ConfigProps configProps,
                                        long projectId,
                                        long serviceDefId,
                                        long clusterId,
                                        String elasticsearchHosts);
    RestResponse deletePodLogMonitoring(ConfigProps configProps, long serviceId);
}
