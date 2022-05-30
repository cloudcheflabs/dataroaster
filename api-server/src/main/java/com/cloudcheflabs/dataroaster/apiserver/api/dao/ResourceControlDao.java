package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;

import java.util.List;

public interface ResourceControlDao {
    List<StorageClass> listStorageClasses(Kubeconfig kubeconfig);
    String getExternalIpOfIngressControllerNginx(Kubeconfig kubeconfig, String namespace);
}
