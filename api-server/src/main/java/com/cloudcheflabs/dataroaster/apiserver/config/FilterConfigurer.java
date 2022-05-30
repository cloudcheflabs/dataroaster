package com.cloudcheflabs.dataroaster.apiserver.config;

import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
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
        registrationBean.addUrlPatterns("/public/*");
        registrationBean.addUrlPatterns("/apis/*");

        return registrationBean;
    }
}
