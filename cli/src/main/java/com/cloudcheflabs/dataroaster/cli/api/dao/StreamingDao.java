package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface StreamingDao {

    RestResponse createStreaming(ConfigProps configProps,
                            long projectId,
                            long serviceDefId,
                            long clusterId,
                            int kafkaReplicaCount,
                            int kafkaStorageSize,
                            String storageClass,
                            int zkReplicaCount);
    RestResponse deleteStreaming(ConfigProps configProps, long serviceId);
}
