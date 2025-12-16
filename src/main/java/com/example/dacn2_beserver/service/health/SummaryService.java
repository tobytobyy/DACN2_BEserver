package com.example.dacn2_beserver.service.health;

import com.example.dacn2_beserver.dto.health.DailyAggregateResponse;
import com.example.dacn2_beserver.model.health.DailyAggregate;
import com.example.dacn2_beserver.repository.DailyAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final DailyAggregateRepository dailyAggregateRepository;

    public DailyAggregateResponse getByDate(String userId, LocalDate date) {
        DailyAggregate agg = dailyAggregateRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> DailyAggregate.builder().userId(userId).date(date).build());
        return toResponse(agg);
    }

    public List<DailyAggregateResponse> getRange(String userId, LocalDate from, LocalDate to) {
        return dailyAggregateRepository.findAllByUserIdAndDateBetween(userId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DailyAggregateResponse toResponse(DailyAggregate a) {
        return DailyAggregateResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .date(a.getDate())
                .steps(a.getSteps())
                .distanceKm(a.getDistanceKm())
                .caloriesIn(a.getCaloriesIn())
                .caloriesOut(a.getCaloriesOut())
                .avgHeartRate(a.getAvgHeartRate())
                .maxHeartRate(a.getMaxHeartRate())
                .minHeartRate(a.getMinHeartRate())
                .waterMl(a.getWaterMl())
                .sleepMinutes(a.getSleepMinutes())
                .deepMinutes(a.getDeepMinutes())
                .remMinutes(a.getRemMinutes())
                .lightMinutes(a.getLightMinutes())
                .awakeMinutes(a.getAwakeMinutes())
                .highlights(a.getHighlights())
                .summary(a.getSummary())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}