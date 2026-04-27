package com.savefrut.api.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.savefrut.api")
@EntityScan(basePackages = "com.savefrut.api.common.entity")
@EnableJpaRepositories(basePackages = "com.savefrut.api.common.repository")
public class SavefrutApiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SavefrutApiAdminApplication.class, args);
    }
}