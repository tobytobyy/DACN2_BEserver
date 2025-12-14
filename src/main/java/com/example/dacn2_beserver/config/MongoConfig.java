package com.example.dacn2_beserver.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        return MongoClients.create(uri);
    }

    /**
     * Lấy database name từ URI nếu có, nếu không sẽ fallback "test".
     * Bạn có thể đổi default này theo ý (vd: "dacn2").
     */
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(MongoClient mongoClient,
                                       @Value("${spring.data.mongodb.uri}") String uri) {
        String dbName = "test";
        int slash = uri.lastIndexOf('/');
        if (slash > -1 && slash < uri.length() - 1) {
            String tail = uri.substring(slash + 1);
            int q = tail.indexOf('?');
            dbName = (q > -1) ? tail.substring(0, q) : tail;
            if (dbName.isBlank()) dbName = "test";
        }
        return new MongoTemplate(mongoClient, dbName);
    }
}