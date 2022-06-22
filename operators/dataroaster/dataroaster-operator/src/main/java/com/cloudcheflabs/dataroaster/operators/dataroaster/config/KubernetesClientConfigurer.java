package com.cloudcheflabs.dataroaster.operators.dataroaster.config;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientConfigurer {

    public static final String PROPERTY_DATAROASTER_KUBECONFIG = "dataroasterKubeconfig";

    @Bean
    public KubernetesClient kubernetesClient() {
        String kubeconfig = System.getProperty(PROPERTY_DATAROASTER_KUBECONFIG);
        if(kubeconfig != null) {
            String kubeConfigYaml = FileUtils.fileToString(kubeconfig, false);
            try {
                Config config = Config.fromKubeconfig(kubeConfigYaml);
                return new DefaultKubernetesClient(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new DefaultKubernetesClient();
        }
    }
}
