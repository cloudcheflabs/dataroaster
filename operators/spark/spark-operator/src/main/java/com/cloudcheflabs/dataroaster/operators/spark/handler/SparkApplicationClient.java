package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplicationList;
import com.cloudcheflabs.dataroaster.operators.spark.util.KubernetesUtils;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class SparkApplicationClient {

    private KubernetesClient client;
    private NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> sparkApplicationClient;

    public SparkApplicationClient(KubernetesClient client) {
        this(client, true);
    }

    public SparkApplicationClient(KubernetesClient client, boolean resourceNamespaced) {
        this.client = client;
        String namespace = KubernetesUtils.getNamespace();
        sparkApplicationClient = client.customResources(SparkApplication.class, SparkApplicationList.class);
        if (resourceNamespaced) {
            sparkApplicationClient = ((MixedOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>>) sparkApplicationClient).inNamespace(namespace);
        }
    }

    public NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> getSparkApplicationClient() {
        return sparkApplicationClient;
    }
}
