package com.savefrut.mspuntos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsPuntosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPuntosApplication.class, args);
    }
}
