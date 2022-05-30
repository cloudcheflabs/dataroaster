package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface MetricsMonitoringService {
    void create(long projectId, long serviceDefId, long clusterId, String userName, String storageClass, int storageSize);
    void delete(long serviceId, String userName);
}
