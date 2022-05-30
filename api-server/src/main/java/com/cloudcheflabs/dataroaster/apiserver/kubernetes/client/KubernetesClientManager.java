package com.cloudcheflabs.dataroaster.apiserver.kubernetes.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesClientManager {

    public static KubernetesClient newClient(String masterUrl,
                                             String clusterCertData,
                                             String clientCertData,
                                             String clientKeyData) {

        System.setProperty("kubernetes.disable.autoConfig", "true");
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "false");
        System.setProperty("kubernetes.tryNamespacePath", "false");


        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withCaCertData(clusterCertData)
                .withClientCertData(clientCertData)
                .withClientKeyData(clientKeyData)
                .build();
        return new DefaultKubernetesClient(config);
    }
}
