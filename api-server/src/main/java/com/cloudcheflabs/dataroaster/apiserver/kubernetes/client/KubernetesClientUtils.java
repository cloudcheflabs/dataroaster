package com.cloudcheflabs.dataroaster.apiserver.kubernetes.client;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesClientUtils {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesClientUtils.class);

    public static KubernetesClient newClient(Kubeconfig kubeconfig) {
        String masterUrl = kubeconfig.getMasterUrl();
        String clusterCertData = kubeconfig.getClusterCertData();
        String clientCertData = kubeconfig.getClientCertData();
        String clientKeyData = kubeconfig.getClientKeyData();
        return KubernetesClientManager.newClient(masterUrl, clusterCertData, clientCertData, clientKeyData);
    }

    /**
     * build kubernetes client with the kubeconfig yaml contents.
     *
     * @param kubeconfig
     * @return
     */
    public static KubernetesClient newClientWithKubeconfigYaml(Kubeconfig kubeconfig) {
        try {
            Config config = Config.fromKubeconfig(kubeconfig.getRawKubeconfig());
            return new DefaultKubernetesClient(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
