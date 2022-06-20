package com.cloudcheflabs.dataroaster.operators.helm.config;


import com.cloudcheflabs.dataroaster.operators.helm.handler.HelmChartClient;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelmConfig {

    @Bean
    public HelmChartClient helmChartClient() { return new HelmChartClient(kubernetesClient()); }

    @Bean
    public KubernetesClient kubernetesClient() { return new DefaultKubernetesClient(); }

}
