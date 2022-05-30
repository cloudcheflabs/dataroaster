package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface KubeconfigDao {
    RestResponse createKubeconfig(ConfigProps configProps, long id, String kubeconfig);
    RestResponse updateKubeconfig(ConfigProps configProps, long id, String kubeconfig);
}
