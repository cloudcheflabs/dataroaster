package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface CiCdDao {
    RestResponse createCiCd(ConfigProps configProps,
                            long projectId,
                            long serviceDefId,
                            long clusterId,
                            String argocdIngressHost,
                            String jenkinsIngressHost,
                            String storageClass);
    RestResponse deleteCiCd(ConfigProps configProps, long serviceId);
}
