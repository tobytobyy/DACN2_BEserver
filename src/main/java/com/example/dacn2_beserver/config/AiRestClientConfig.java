package com.example.dacn2_beserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiRestClientConfig {

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.food.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${ai.food.read-timeout-ms:60000}")
    private int readTimeoutMs;

    @Bean
    public RestClient aiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(aiBaseUrl)
                .requestFactory(factory)
                .build();
    }
}