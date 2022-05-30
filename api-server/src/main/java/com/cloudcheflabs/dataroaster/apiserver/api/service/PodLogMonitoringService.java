package com.cloudcheflabs.dataroaster.apiserver.api.service;

import java.util.List;

public interface PodLogMonitoringService {
    void create(long projectId, long serviceDefId, long clusterId, String userName, List<String> elasticsearchHosts);
    void delete(long serviceId, String userName);
}
