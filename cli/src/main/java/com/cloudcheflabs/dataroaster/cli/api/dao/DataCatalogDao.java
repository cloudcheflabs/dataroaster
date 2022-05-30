package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface DataCatalogDao {

    RestResponse createDataCatalog(ConfigProps configProps,
                            long projectId,
                            long serviceDefId,
                            long clusterId,
                            String s3Bucket,
                            String s3AccessKey,
                            String s3SecretKey,
                            String s3Endpoint,
                            String storageClass,
                            int storageSize);
    RestResponse deleteDataCatalog(ConfigProps configProps, long serviceId);
}
