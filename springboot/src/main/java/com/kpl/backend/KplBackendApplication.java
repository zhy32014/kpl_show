package com.kpl.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KplBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(KplBackendApplication.class, args);
    }
}
