package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface AnalyticsService {

    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String jupyterhubGithubClientId,
                String jupyterhubGithubClientSecret,
                String jupyterhubIngressHost,
                String storageClass,
                int jupyterhubStorageSize,
                int redashStorageSize);
    void delete(long serviceId, String userName);
}
