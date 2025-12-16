package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.health.*;
import com.example.dacn2_beserver.security.AuthPrincipal;
import com.example.dacn2_beserver.service.health.SleepService;
import com.example.dacn2_beserver.service.health.SummaryService;
import com.example.dacn2_beserver.service.health.WaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final WaterService waterService;
    private final SummaryService summaryService;
    private final SleepService sleepService;

    // -------- WATER --------

    @PostMapping("/water")
    public WaterLogResponse createWater(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateWaterLogRequest req
    ) {
        return waterService.create(principal.userId(), req);
    }

    @GetMapping("/water")
    public List<WaterLogResponse> listWater(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return waterService.listResponses(principal.userId(), from, to);
    }

    // -------- SLEEP --------

    @PostMapping("/sleep")
    public SleepSessionResponse createSleep(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateSleepSessionRequest req
    ) {
        return sleepService.create(principal.userId(), req);
    }

    @GetMapping("/sleep")
    public List<SleepSessionResponse> listSleep(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return sleepService.listResponses(principal.userId(), from, to);
    }

    // -------- SUMMARY --------

    @GetMapping("/summary/{date}")
    public DailyAggregateResponse summaryByDate(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return summaryService.getByDate(principal.userId(), date);
    }

    @GetMapping("/summary")
    public List<DailyAggregateResponse> summaryRange(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return summaryService.getRange(principal.userId(), from, to);
    }
}