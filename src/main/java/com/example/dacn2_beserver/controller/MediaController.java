package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.common.ApiResponse;
import com.example.dacn2_beserver.dto.media.PresignGetResponse;
import com.example.dacn2_beserver.dto.media.PresignPutRequest;
import com.example.dacn2_beserver.dto.media.PresignPutResponse;
import com.example.dacn2_beserver.security.AuthPrincipal;
import com.example.dacn2_beserver.service.storage.ChatMediaS3Service;
import com.example.dacn2_beserver.service.storage.NutritionS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final NutritionS3Service nutritionS3Service;
    private final ChatMediaS3Service chatMediaS3Service;

    @Value("${aws.s3.chat.presign.get-ttl-seconds:600}")
    private long chatGetTtlSeconds;

    /**
     * Client (mobile) request presigned post to upload nutrition media to S3 nutrition bucket.
     * Return: objectKey + uploadUrl + expiresAt
     */
    @PostMapping(value = "/nutrition/presign-put", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PresignPutResponse> presignNutritionPut(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody PresignPutRequest req
    ) {
        var r = nutritionS3Service.presignPut(
                principal.userId(),
                req.getContentType(),
                req.getSizeBytes() == null ? 0L : req.getSizeBytes()
        );

        return ApiResponse.ok(PresignPutResponse.builder()
                .objectKey(r.objectKey())
                .uploadUrl(r.uploadUrl())
                .expiresAt(r.expiresAt())
                .publicUrl(null)
                .build());
    }

    /**
     * Client (mobile) request presigned PUT to upload chat media to S3 chat bucket.
     * Return: objectKey + uploadUrl + expiresAt
     */
    @PostMapping(value = "/chat/presign-put", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PresignPutResponse> presignChatPut(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody PresignPutRequest req
    ) {
        var r = chatMediaS3Service.presignPut(
                principal.userId(),
                req.getContentType(),
                req.getSizeBytes() == null ? 0L : req.getSizeBytes()
        );

        return ApiResponse.ok(PresignPutResponse.builder()
                .objectKey(r.objectKey())
                .uploadUrl(r.uploadUrl())
                .expiresAt(r.expiresAt())
                .publicUrl(chatMediaS3Service.publicUrl(r.objectKey()))
                .build());
    }

    /**
     * Client request presigned GET to get chat media from S3 chat bucket (private).
     * Returns: url + expiresAt
     */
    @GetMapping("/chat/presign-get")
    public ApiResponse<PresignGetResponse> presignChatGet(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam String objectKey
    ) {
        chatMediaS3Service.assertOwnedByUser(principal.userId(), objectKey);

        String url = chatMediaS3Service.presignGetUrl(objectKey);
        Instant expiresAt = Instant.now().plusSeconds(chatGetTtlSeconds);

        return ApiResponse.ok(PresignGetResponse.builder()
                .url(url)
                .expiresAt(expiresAt)
                .build());
    }
}