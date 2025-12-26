package com.example.dacn2_beserver.service.nutrition;

import com.example.dacn2_beserver.dto.nutrition.ConfirmFoodLogRequest;
import com.example.dacn2_beserver.dto.nutrition.FoodLogResponse;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import com.example.dacn2_beserver.model.common.SourceMeta;
import com.example.dacn2_beserver.model.enums.DataSource;
import com.example.dacn2_beserver.model.enums.RecordStatus;
import com.example.dacn2_beserver.model.health.FoodItem;
import com.example.dacn2_beserver.model.health.FoodLog;
import com.example.dacn2_beserver.repository.FoodItemRepository;
import com.example.dacn2_beserver.repository.FoodLogRepository;
import com.example.dacn2_beserver.service.health.DailyAggregateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodLogService {

    private final FoodLogRepository foodLogRepository;
    private final FoodItemRepository foodItemRepository;
    private final DailyAggregateService dailyAggregateService;

    public FoodLogResponse confirm(String userId, ConfirmFoodLogRequest req) {
        String code = req != null ? req.getFoodCode() : null;
        if (code == null || code.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "foodCode is required");
        }

        Instant now = Instant.now();

        FoodItem item = foodItemRepository.findByCode(code.trim()).orElse(null);

        FoodLog log = FoodLog.builder()
                .userId(userId)
                .loggedAt(now)
                .label(code.trim())
                .confidence(req.getConfidence())
                .foodItemId(item != null ? item.getId() : null)
                .kcal(item != null ? item.getCalories() : null)
                .carbs(item != null ? item.getCarbs() : null)
                .fat(item != null ? item.getFat() : null)
                .protein(item != null ? item.getProtein() : null)
                .status(RecordStatus.CONFIRMED)
                .meta(SourceMeta.builder()
                        .source(DataSource.AI_INFERRED)
                        .build())
                .build();

        log = foodLogRepository.save(log);

        boolean added = false;
        if (log.getKcal() != null) {
            dailyAggregateService.addCaloriesIn(userId, now, log.getKcal());
            added = true;
        }

        return toResponse(log, item, added);
    }

    public void delete(String userId, String id) {
        if (id == null || id.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "id is required");
        }

        FoodLog log = foodLogRepository.findByIdAndUserId(id.trim(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "FoodLog not found"));

        Integer kcal = log.getKcal();
        if (kcal != null && log.getLoggedAt() != null) {
            dailyAggregateService.addCaloriesIn(userId, log.getLoggedAt(), -kcal);
        }

        foodLogRepository.deleteById(log.getId());
    }

    public List<FoodLogResponse> list(String userId, Instant from, Instant to) {
        if (from == null || to == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "from and to are required");
        }
        if (to.isBefore(from)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "to must be >= from");
        }

        return foodLogRepository.findAllByUserIdAndLoggedAtBetweenOrderByLoggedAtDesc(userId, from, to)
                .stream()
                .map(l -> {
                    FoodItem item = (l.getFoodItemId() == null) ? null : foodItemRepository.findById(l.getFoodItemId()).orElse(null);
                    return toResponse(l, item, false);
                })
                .toList();
    }

    private FoodLogResponse toResponse(FoodLog log, FoodItem item, boolean addedToAgg) {
        return FoodLogResponse.builder()
                .id(log.getId())
                .loggedAt(log.getLoggedAt())
                .label(log.getLabel())
                .confidence(log.getConfidence())
                .foodItemId(log.getFoodItemId())
                .foodCode(item != null ? item.getCode() : log.getLabel())
                .foodName(item != null ? item.getName() : null)
                .kcal(log.getKcal())
                .carbs(log.getCarbs())
                .fat(log.getFat())
                .protein(log.getProtein())
                .addedToDailyAggregate(addedToAgg)
                .build();
    }
}