package com.trinity.ctc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CtcApplication {
    public static void main(String[] args) {
        SpringApplication.run(CtcApplication.class, args);
    }
}
