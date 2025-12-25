package com.example.dacn2_beserver;

import com.example.dacn2_beserver.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateLimitProperties.class)
public class Dacn2BEserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dacn2BEserverApplication.class, args);
    }
}