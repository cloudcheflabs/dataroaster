package com.cloudcheflabs.dataroaster.trino.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;


@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class TrinoGatewayApplication {

	private static Logger LOG = LoggerFactory.getLogger(TrinoGatewayApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TrinoGatewayApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			LOG.info("beans loaded by the spring boot application: ");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				LOG.info("- bean name: {}", beanName);
			}
		};
	}
}
