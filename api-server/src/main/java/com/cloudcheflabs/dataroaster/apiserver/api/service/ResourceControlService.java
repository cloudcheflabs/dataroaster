package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;

import java.util.List;

public interface ResourceControlService {
    List<StorageClass> listStorageClasses(long clusterId, String userName);
    String getExternalIpOfIngressControllerNginx(long clusterId, String userName);
}
