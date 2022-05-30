package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface QueryEngineDao {

    RestResponse createQueryEngine(ConfigProps configProps,
                                   long projectId,
                                   long serviceDefId,
                                   long clusterId,
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
    RestResponse deleteQueryEngine(ConfigProps configProps, long serviceId);
}
