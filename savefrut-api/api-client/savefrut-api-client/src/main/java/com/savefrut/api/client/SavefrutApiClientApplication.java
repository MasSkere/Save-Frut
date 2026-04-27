package com.savefrut.api.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.savefrut.api.client",
		"com.savefrut.api.common"
})

@EntityScan(basePackages = "com.savefrut.api.common.entity")
@EnableJpaRepositories(basePackages = "com.savefrut.api.common.repository")
public class SavefrutApiClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SavefrutApiClientApplication.class, args);
	}
}