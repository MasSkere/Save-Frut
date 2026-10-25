package com.savefrut.mscatalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableCaching
@EnableFeignClients
@EnableMethodSecurity
@SpringBootApplication
public class MsCatalogoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsCatalogoApplication.class, args);
    }
}
