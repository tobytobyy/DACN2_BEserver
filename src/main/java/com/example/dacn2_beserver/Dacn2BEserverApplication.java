package com.example.dacn2_beserver;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Dacn2BEserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dacn2BEserverApplication.class, args);
    }
    @PostConstruct
    public void checkEnv() {
        System.out.println("MONGODB_URI = " + System.getenv("MONGODB_URI"));
    }
}
