package com.cloudcheflabs.dataroaster.authorizer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Value("${resource.id:spring-boot-application}")
	private String resourceId;
	
	@Value("${security.oauth2.resource.jwt.publicKeyPath}")
    private String publicKeyPath;


	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
        		.antMatchers("/api/**")
				.hasAnyRole("PLATFORM_ADMIN", "GROUP_ADMIN", "USER");
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {		
		resources.resourceId(resourceId);
	}


}
