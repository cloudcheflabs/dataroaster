package com.cloudcheflabs.dataroaster.apiserver.api.dao;


import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;

public interface K8sNamespaceDao extends Operations<K8sNamespace> {

    K8sNamespace findByNameAndClusterId(String namespaceName, long clusterId);

}
