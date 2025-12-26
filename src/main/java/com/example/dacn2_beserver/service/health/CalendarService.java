package com.example.dacn2_beserver.service.health;

import com.example.dacn2_beserver.dto.health.CalendarDaySummaryResponse;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import com.example.dacn2_beserver.model.health.DailyAggregate;
import com.example.dacn2_beserver.repository.DailyAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final DailyAggregateRepository dailyAggregateRepository;

    public CalendarDaySummaryResponse getDay(String userId, LocalDate date) {
        if (date == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "date is required");
        }

        LocalDate today = LocalDate.now();
        boolean isFuture = date.isAfter(today);

        // For future days: return default zeros, no DB query needed.
        if (isFuture) {
            return emptyDay(date, true);
        }

        DailyAggregate a = dailyAggregateRepository.findByUserIdAndDate(userId, date).orElse(null); // supported :contentReference[oaicite:1]{index=1}
        if (a == null) {
            return emptyDay(date, false);
        }

        return CalendarDaySummaryResponse.builder()
                .date(date)
                .steps(nvl(a.getSteps()))
                .distanceKm(nvl(a.getDistanceKm()))
                .caloriesIn(nvl(a.getCaloriesIn()))
                .caloriesOut(nvl(a.getCaloriesOut()))
                .waterMl(nvl(a.getWaterMl()))
                .sleepMinutes(nvl(a.getSleepMinutes()))
                .deepMinutes(nvl(a.getDeepMinutes()))
                .remMinutes(nvl(a.getRemMinutes()))
                .lightMinutes(nvl(a.getLightMinutes()))
                .awakeMinutes(nvl(a.getAwakeMinutes()))
                .highlights(a.getHighlights() != null ? a.getHighlights() : List.of())
                .summary(a.getSummary())
                .isFuture(false)
                .build();
    }

    private CalendarDaySummaryResponse emptyDay(LocalDate date, boolean isFuture) {
        return CalendarDaySummaryResponse.builder()
                .date(date)
                .steps(0)
                .distanceKm(0.0)
                .caloriesIn(0)
                .caloriesOut(0)
                .waterMl(0)
                .sleepMinutes(0)
                .deepMinutes(0)
                .remMinutes(0)
                .lightMinutes(0)
                .awakeMinutes(0)
                .highlights(List.of())
                .summary(null)
                .isFuture(isFuture)
                .build();
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }
}