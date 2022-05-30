package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface PrivateRegistryService {
    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String coreHost,
                String notaryHost,
                String storageClass,
                int registryStorageSize,
                int chartmuseumStorageSize,
                int jobserviceStorageSize,
                int databaseStorageSize,
                int redisStorageSize,
                int trivyStorageSize,
                String s3Bucket,
                String s3AccessKey,
                String s3SecretKey,
                String s3Endpoint);
    void delete(long serviceId, String userName);
}
