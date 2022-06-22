package com.cloudcheflabs.dataroaster.operators.dataroaster.config;

import com.cloudcheflabs.dataroaster.operators.dataroaster.filter.AuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfigurer {

    @Autowired
    private AuthorizationFilter authorizationFilter;

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilterFilterRegistrationBean(){
        FilterRegistrationBean<AuthorizationFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(authorizationFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
