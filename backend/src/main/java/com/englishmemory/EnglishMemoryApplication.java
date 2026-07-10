package com.englishmemory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EnglishMemoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishMemoryApplication.class, args);
    }
}
