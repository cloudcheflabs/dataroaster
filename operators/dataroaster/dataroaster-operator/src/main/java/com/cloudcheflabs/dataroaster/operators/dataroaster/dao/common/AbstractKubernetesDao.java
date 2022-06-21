package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractKubernetesDao {

    @Autowired
    protected KubernetesClient kubernetesClient;
}
