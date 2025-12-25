package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.media.PresignPutRequest;
import com.example.dacn2_beserver.dto.media.PresignPutResponse;
import com.example.dacn2_beserver.security.AuthPrincipal;
import com.example.dacn2_beserver.service.storage.NutritionS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final NutritionS3Service nutritionS3Service;

    @PostMapping(value = "/nutrition/presign-put", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PresignPutResponse presignNutritionPut(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody PresignPutRequest req
    ) {
        var r = nutritionS3Service.presignPut(
                principal.userId(),
                req.getContentType(),
                req.getSizeBytes() == null ? 0 : req.getSizeBytes()
        );

        return PresignPutResponse.builder()
                .objectKey(r.objectKey())
                .uploadUrl(r.uploadUrl())
                .expiresAt(r.expiresAt())
                .build();
    }
}