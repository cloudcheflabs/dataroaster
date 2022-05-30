package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface CiCdService {
    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String argocdIngressHost,
                String jenkinsIngressHost,
                String storageClass);
    void delete(long serviceId, String userName);
}
