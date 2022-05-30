package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface IngressControllerDao {

    RestResponse createIngressController(ConfigProps configProps,
                                       long projectId,
                                       long serviceDefId,
                                       long clusterId);
    RestResponse deleteIngressController(ConfigProps configProps, long serviceId);
    RestResponse getExternalIpOfIngressControllerNginx(ConfigProps configProps, long clusterId);
}
