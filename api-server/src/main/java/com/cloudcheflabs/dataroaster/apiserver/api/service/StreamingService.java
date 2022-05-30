package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface StreamingService {

    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                int kafkaReplicaCount,
                int kafkaStorageSize,
                String storageClass,
                int zkReplicaCount);
    void delete(long serviceId, String userName);
}
