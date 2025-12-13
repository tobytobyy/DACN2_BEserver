package com.example.dacn2_beserver.repository;

import com.example.dacn2_beserver.model.ai.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findTop100BySessionIdOrderByCreatedAtAsc(String sessionId);

    void deleteAllBySessionId(String sessionId);
}