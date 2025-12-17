package com.example.dacn2_beserver.repository;

import com.example.dacn2_beserver.model.health.FoodLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface FoodLogRepository extends MongoRepository<FoodLog, String> {
    List<FoodLog> findAllByUserIdAndLoggedAtBetweenOrderByLoggedAtDesc(String userId, Instant from, Instant to);
}