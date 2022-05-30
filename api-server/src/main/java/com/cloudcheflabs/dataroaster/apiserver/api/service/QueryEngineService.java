package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface QueryEngineService {

    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String s3Bucket,
                String s3AccessKey,
                String s3SecretKey,
                String s3Endpoint,
                String sparkThriftServerStorageClass,
                int sparkThriftServerExecutors,
                int sparkThriftServerExecutorMemory,
                int sparkThriftServerExecutorCores,
                int sparkThriftServerDriverMemory,
                int trinoWorkers,
                int trinoServerMaxMemory,
                int trinoCores,
                int trinoTempDataStorage,
                int trinoDataStorage,
                String trinoStorageClass);
    void delete(long serviceId, String userName);
}
