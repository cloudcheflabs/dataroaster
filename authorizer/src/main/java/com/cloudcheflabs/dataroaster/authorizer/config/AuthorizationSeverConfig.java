package com.cloudcheflabs.dataroaster.authorizer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;


@Configuration
@EnableAuthorizationServer
public class AuthorizationSeverConfig extends AuthorizationServerConfigurerAdapter{

	private static Logger LOG = LoggerFactory.getLogger(AuthorizationSeverConfig.class);

	@Value("${resouce.id:spring-boot-application}")
	private String resourceId;

	@Value("${security.oauth2.resource.jwt.keypair.path}")
	private String keyPairPath;

	@Value("${security.oauth2.resource.jwt.keypair.alias}")
	private String keyPairAlias;

	@Value("${security.oauth2.resource.jwt.keypair.keypass}")
	private String keypass;

	@Value("${security.oauth2.resource.jwt.accessTokenValiditySeconds}")
	private int accessTokenValiditySeconds;

	@Value("${security.oauth2.resource.jwt.refreshTokenValiditySeconds}")
	private int refreshTokenValiditySeconds;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;


	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Bean
	public TokenStore tokenStore() {
		return new JdbcTokenStore(jdbcTemplate.getDataSource());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

		KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource(keyPairPath), keypass.toCharArray())
				.getKeyPair(keyPairAlias, keypass.toCharArray());
		converter.setKeyPair(keyPair);

		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenService() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setTokenEnhancer(accessTokenConverter());
		defaultTokenServices.setSupportRefreshToken(true);
		defaultTokenServices.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
		defaultTokenServices.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
		return defaultTokenServices;
	}

	@Bean
	@Primary
	public JdbcClientDetailsService jdbcClientDetailsService() {
		return new JdbcClientDetailsService(jdbcTemplate.getDataSource());
	}


	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore())
				.tokenServices(tokenService())
				.authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(jdbcClientDetailsService());
	}
}
