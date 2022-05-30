package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface DistributedTracingService {
    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String storageClass,
                String ingressHost,
                String elasticsearchHostPort);
    void delete(long serviceId, String userName);
}
