package com.cloudcheflabs.dataroaster.operators.trino.config;


import com.cloudcheflabs.dataroaster.operators.trino.api.dao.ResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.dao.KubernetesResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.handler.TrinoClusterClient;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeanConfig {
    @Bean
    public ResourceDao resourceDao() { return new KubernetesResourceDao(kubernetesClient()); }

    @Bean
    public TrinoClusterClient trinoClusterClient() { return new TrinoClusterClient(kubernetesClient()); }

    @Bean
    public KubernetesClient kubernetesClient() { return new DefaultKubernetesClient(); }

}
