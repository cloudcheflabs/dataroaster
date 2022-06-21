package com.cloudcheflabs.dataroaster.operators.dataroaster.kubernetes.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesClientUtils {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesClientUtils.class);

    public static KubernetesClient newClientWithKubeconfig(String contents) {
        try {
            Config config = Config.fromKubeconfig(contents);
            return new DefaultKubernetesClient(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
