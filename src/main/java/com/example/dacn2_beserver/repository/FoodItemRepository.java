package com.example.dacn2_beserver.repository;

import com.example.dacn2_beserver.model.health.FoodItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FoodItemRepository extends MongoRepository<FoodItem, String> {
    List<FoodItem> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
}