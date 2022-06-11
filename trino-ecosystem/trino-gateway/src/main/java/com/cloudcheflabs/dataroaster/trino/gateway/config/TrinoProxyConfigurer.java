package com.cloudcheflabs.dataroaster.trino.gateway.config;

import com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrinoProxyConfigurer {

    @Bean
    public TrinoProxy trinoProxy()
    {
        TrinoProxy trinoProxy = new TrinoProxy();
        return trinoProxy;
    }

}
