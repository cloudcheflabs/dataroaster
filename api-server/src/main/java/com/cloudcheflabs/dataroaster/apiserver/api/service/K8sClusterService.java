package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;

public interface K8sClusterService extends Operations<K8sCluster> {

    void createCluster(String clusterName, String description);
    void updateCluster(long id, String clusterName, String description);
    void deleteCluster(long id);
    void createKubeconfig(long id, String kubeconfig, String userName);
    void updateKubeconfig(long id, String kubeconfig, String userName);
    Kubeconfig getKubeconfig(long clusterId, String userName);
}
