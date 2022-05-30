package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface AnalyticsDao {

    RestResponse createAnalytics(ConfigProps configProps,
                                 long projectId,
                                 long serviceDefId,
                                 long clusterId,
                                 String jupyterhubGithubClientId,
                                 String jupyterhubGithubClientSecret,
                                 String jupyterhubIngressHost,
                                 String storageClass,
                                 int jupyterhubStorageSize,
                                 int redashStorageSize);
    RestResponse deleteAnalytics(ConfigProps configProps, long serviceId);
}
