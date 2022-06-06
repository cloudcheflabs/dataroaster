package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class TrinoClusterClient {
    private KubernetesClient client;
    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;

    public TrinoClusterClient(KubernetesClient client) {
        this.client = client;
        trinoClusterClient = client.resources(TrinoCluster.class);
    }

    public KubernetesClient getClient() {
        return client;
    }

    public MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> getTrinoClusterClient() {
        return trinoClusterClient;
    }
}
