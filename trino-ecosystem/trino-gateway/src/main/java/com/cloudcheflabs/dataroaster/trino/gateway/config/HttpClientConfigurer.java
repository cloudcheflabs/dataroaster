package com.cloudcheflabs.dataroaster.trino.gateway.config;

import com.cloudcheflabs.dataroaster.trino.gateway.component.SimpleHttpClient;
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
