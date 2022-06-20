package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class HelmChartClient {
    private KubernetesClient client;
    private MixedOperation<HelmChart, KubernetesResourceList<HelmChart>, Resource<HelmChart>> helmChartClient;

    public HelmChartClient(KubernetesClient client) {
        this.client = client;
        helmChartClient = client.resources(HelmChart.class);
    }

    public KubernetesClient getClient() {
        return client;
    }

    public MixedOperation<HelmChart, KubernetesResourceList<HelmChart>, Resource<HelmChart>> getHelmChartClient() {
        return helmChartClient;
    }
}
