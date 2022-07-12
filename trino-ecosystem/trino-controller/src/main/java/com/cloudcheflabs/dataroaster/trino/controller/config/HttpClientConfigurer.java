package com.cloudcheflabs.dataroaster.trino.controller.config;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.trino.controller.component.SimpleHttpClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfigurer {

    private static Logger LOG = LoggerFactory.getLogger(HttpClientConfigurer.class);


    @Bean
    public SimpleHttpClient simpleHttpClient() {
        return new SimpleHttpClient();
    }
}
