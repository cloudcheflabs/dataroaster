package com.cloudcheflabs.dataroaster.operators.dataroaster;

import com.cloudcheflabs.dataroaster.operators.dataroaster.component.DBSchemaCreator;
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
public class DataRoasterApplication {

	private static Logger LOG = LoggerFactory.getLogger(DataRoasterApplication.class);

	public static void main(String[] args) {
		String createMySQLDBSchema = System.getProperty("createMySQLDBSchema");
		if(createMySQLDBSchema != null) {
			LOG.info("createMySQLDBSchema: [{}]", createMySQLDBSchema);
			DBSchemaCreator.main(args);
		} else {
			SpringApplication.run(DataRoasterApplication.class, args);
		}
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			LOG.debug("beans loaded by the spring boot application: ");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				LOG.debug("- bean name: {}", beanName);
			}
		};
	}
}
