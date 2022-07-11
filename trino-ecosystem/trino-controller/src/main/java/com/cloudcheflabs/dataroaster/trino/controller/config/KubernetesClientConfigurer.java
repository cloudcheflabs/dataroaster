package com.cloudcheflabs.dataroaster.trino.controller.config;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientConfigurer {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesClientConfigurer.class);

    public static final String PROPERTY_TRINO_CONTROLLER_KUBECONFIG = "trinoControllerKubeconfig";

    @Bean
    public KubernetesClient kubernetesClient() {
        String kubeconfig = System.getProperty(PROPERTY_TRINO_CONTROLLER_KUBECONFIG);
        if(kubeconfig != null) {
            String kubeConfigYaml = FileUtils.fileToString(kubeconfig, false);
            try {
                LOG.info("kubernetes client instance being created with kubeconfig [{}]...", kubeconfig);
                Config config = Config.fromKubeconfig(kubeConfigYaml);
                return new DefaultKubernetesClient(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            LOG.info("default kubernetes client instance being created...");
            return new DefaultKubernetesClient();
        }
    }
}
