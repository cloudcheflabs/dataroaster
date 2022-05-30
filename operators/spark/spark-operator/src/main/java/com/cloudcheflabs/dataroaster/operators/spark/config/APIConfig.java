package com.cloudcheflabs.dataroaster.operators.spark.config;


import com.cloudcheflabs.dataroaster.operators.spark.api.dao.ResourceDao;
import com.cloudcheflabs.dataroaster.operators.spark.dao.kubernetes.KubernetesResourceDao;
import com.cloudcheflabs.dataroaster.operators.spark.handler.SparkApplicationClient;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIConfig {
    @Bean
    public ResourceDao resourceDao() { return new KubernetesResourceDao(kubernetesClient()); }

    @Bean
    public SparkApplicationClient sparkApplicationClient() { return new SparkApplicationClient(kubernetesClient()); }

    @Bean
    public KubernetesClient kubernetesClient() { return new DefaultKubernetesClient(); }

}
