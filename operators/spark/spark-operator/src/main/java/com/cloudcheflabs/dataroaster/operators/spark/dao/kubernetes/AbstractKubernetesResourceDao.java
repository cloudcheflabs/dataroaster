package com.cloudcheflabs.dataroaster.operators.spark.dao.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class AbstractKubernetesResourceDao {

    protected KubernetesClient client;

    public AbstractKubernetesResourceDao(KubernetesClient client) {
        this.client = client;
    }
}
