package com.astrotech.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = "com.astrotech.transport.repositories")
@EntityScan(basePackages = "com.astrotech.transport.entities")
public class TransportApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportApplication.class, args);
	}

}
