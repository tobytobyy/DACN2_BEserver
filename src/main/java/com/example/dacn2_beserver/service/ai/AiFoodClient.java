package com.example.dacn2_beserver.service.ai;

import com.example.dacn2_beserver.dto.ai.AiFoodPredictResponse;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AiFoodClient {

    private final RestClient restClient = RestClient.create();

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.food.predict-path}")
    private String predictPath;

    public AiFoodPredictResponse predictFood(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Image is required");
        }

        try {
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null
                            ? image.getOriginalFilename()
                            : "food.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            return restClient.post()
                    .uri(baseUrl + predictPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(AiFoodPredictResponse.class);

        } catch (Exception e) {
            throw new ApiException(
                    ErrorCode.AI_SERVICE_ERROR,
                    "Failed to call AI server: " + e.getMessage()
            );
        }
    }
}