package com.aequitas.aequitascentralservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Aequitas Central Service Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class AequitasCentralServiceApplication {

    /**
     * Boots the Spring context.
     *
     * @param args CLI arguments handed to Spring Boot.
     */
    public static void main(final String[] args) {
        SpringApplication.run(AequitasCentralServiceApplication.class, args);
    }
}
