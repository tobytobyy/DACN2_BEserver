package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.common.ApiResponse;
import com.example.dacn2_beserver.dto.nutrition.ConfirmFoodLogRequest;
import com.example.dacn2_beserver.dto.nutrition.FoodLogResponse;
import com.example.dacn2_beserver.security.AuthPrincipal;
import com.example.dacn2_beserver.service.nutrition.FoodLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/nutrition/logs")
@RequiredArgsConstructor
public class NutritionLogController {

    private final FoodLogService foodLogService;

    @PostMapping("/confirm")
    public ApiResponse<FoodLogResponse> confirm(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody ConfirmFoodLogRequest req
    ) {
        return ApiResponse.ok(foodLogService.confirm(principal.userId(), req));
    }

    @GetMapping
    public ApiResponse<List<FoodLogResponse>> list(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ApiResponse.ok(foodLogService.list(principal.userId(), from, to));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String id
    ) {
        foodLogService.delete(principal.userId(), id);
        return ApiResponse.ok(null);
    }
}