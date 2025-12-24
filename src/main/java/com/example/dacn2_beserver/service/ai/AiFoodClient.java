package com.example.dacn2_beserver.service.ai;

import com.example.dacn2_beserver.dto.ai.AiFoodPredictRequest;
import com.example.dacn2_beserver.dto.ai.AiFoodPredictResponse;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AiFoodClient {

    private final RestClient aiRestClient;

    @Value("${ai.food.predict-path}")
    private String predictPath;

    @Value("${ai.internal-token:}")
    private String internalToken;

    public AiFoodPredictResponse predictFoodByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "image_url is required");
        }

        try {
            AiFoodPredictRequest req = AiFoodPredictRequest.builder()
                    .imageUrl(imageUrl)
                    .build();

            System.out.println("Sending request: " + new ObjectMapper().writeValueAsString(req));

            RestClient.RequestBodySpec requestSpec = aiRestClient.post()
                    .uri(predictPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (internalToken != null && !internalToken.isBlank()) {
                requestSpec = requestSpec.header("X-Internal-Token", internalToken);
            }

            // Gửi body và retrieve trong một chain riêng
            AiFoodPredictResponse response = requestSpec
                    .body(req)
                    .retrieve()
                    .body(AiFoodPredictResponse.class);

            return response;

        } catch (Exception e) {
            throw new ApiException(ErrorCode.AI_SERVICE_ERROR, "Failed to call AI server: " + e.getMessage());
        }
    }
}