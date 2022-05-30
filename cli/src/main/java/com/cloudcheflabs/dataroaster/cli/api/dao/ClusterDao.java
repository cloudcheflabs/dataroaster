package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface ClusterDao {
    RestResponse createCluster(ConfigProps configProps, String name, String description);
    RestResponse updateCluster(ConfigProps configProps, long id, String name, String description);
    RestResponse deleteCluster(ConfigProps configProps, long id);
    RestResponse listClusters(ConfigProps configProps);
}
