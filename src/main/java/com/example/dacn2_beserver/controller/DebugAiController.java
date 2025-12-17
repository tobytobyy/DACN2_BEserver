package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.ai.AiFoodPredictResponse;
import com.example.dacn2_beserver.service.ai.AiFoodClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/_debug")
@RequiredArgsConstructor
public class DebugAiController {

    private final AiFoodClient aiFoodClient;

    @PostMapping(value = "/ai-food", consumes = "multipart/form-data")
    public AiFoodPredictResponse test(
            @RequestPart("image") MultipartFile image
    ) {
        return aiFoodClient.predictFood(image);
    }
}