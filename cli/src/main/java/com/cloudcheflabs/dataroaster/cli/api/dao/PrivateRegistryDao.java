package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface PrivateRegistryDao {

    RestResponse createPrivateRegistry(ConfigProps configProps,
                                       long projectId,
                                       long serviceDefId,
                                       long clusterId,
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
    RestResponse deletePrivateRegistry(ConfigProps configProps, long serviceId);
}
