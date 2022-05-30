package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface WorkflowDao {

    RestResponse createWorkflow(ConfigProps configProps,
                                long projectId,
                                long serviceDefId,
                                long clusterId,
                                String storageClass,
                                int storageSize,
                                String s3Bucket,
                                String s3AccessKey,
                                String s3SecretKey,
                                String s3Endpoint);
    RestResponse deleteWorkflow(ConfigProps configProps, long serviceId);
}
