package com.byteentropy.aml_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AmlCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(AmlCoreApplication.class, args);
    }
}