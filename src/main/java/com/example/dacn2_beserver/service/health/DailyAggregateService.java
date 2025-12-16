package com.example.dacn2_beserver.service.health;

import com.example.dacn2_beserver.model.health.DailyAggregate;
import com.example.dacn2_beserver.model.user.User;
import com.example.dacn2_beserver.repository.DailyAggregateRepository;
import com.example.dacn2_beserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DailyAggregateService
 * <p>
 * Mục tiêu:
 * - Upsert DailyAggregate theo ngày (theo timezone của user)
 * - Cập nhật các metric: water, sleep
 * - Sinh highlights + summary (rule-based MVP)
 * <p>
 * Quy ước:
 * - Water thuộc về ngày của loggedAt (theo timezone user)
 * - Sleep thuộc về ngày "thức dậy" (endAt) theo timezone user
 * - MVP: cộng dồn (support multiple water logs, multiple sleep sessions/nap trong 1 ngày)
 */
@Service
@RequiredArgsConstructor
public class DailyAggregateService {

    // ===== Defaults (MVP) =====
    private static final String DEFAULT_TZ = "UTC";
    private static final int DEFAULT_WATER_GOAL_ML = 2000;
    private static final int DEFAULT_SLEEP_GOAL_MIN = 420; // 7h
    private final DailyAggregateRepository dailyAggregateRepository;
    private final UserRepository userRepository;

    // ============================================================
    // Water
    // ============================================================

    /**
     * Cộng water vào DailyAggregate của ngày tương ứng.
     *
     * @param userId   user id
     * @param loggedAt thời điểm uống
     * @param deltaMl  số ml cộng thêm (dương). Nếu muốn hỗ trợ xoá log thì truyền số âm.
     */
    public DailyAggregate addWater(String userId, Instant loggedAt, int deltaMl) {
        User user = requireUser(userId);
        ZoneId zoneId = userZone(user);

        LocalDate date = LocalDateTime.ofInstant(loggedAt, zoneId).toLocalDate();
        DailyAggregate agg = findOrCreate(userId, date);

        int current = nvl(agg.getWaterMl());
        agg.setWaterMl(Math.max(0, current + deltaMl));
        touch(agg);

        applyWaterHighlights(user, agg);

        return dailyAggregateRepository.save(agg);
    }

    // ============================================================
    // Sleep
    // ============================================================

    /**
     * Ghi nhận sleep session vào DailyAggregate.
     * Quy ước: session được tính vào ngày "wake-up" (endAt).
     */
    public DailyAggregate addSleep(
            String userId,
            Instant sleepEndAt,
            int totalMinutes,
            int deepMinutes,
            int remMinutes,
            int lightMinutes,
            int awakeMinutes
    ) {
        User user = requireUser(userId);
        ZoneId zoneId = userZone(user);

        LocalDate date = LocalDateTime.ofInstant(sleepEndAt, zoneId).toLocalDate();
        DailyAggregate agg = findOrCreate(userId, date);

        // MVP: cộng dồn để hỗ trợ nap hoặc nhiều session
        agg.setSleepMinutes(nvl(agg.getSleepMinutes()) + clamp0(totalMinutes));
        agg.setDeepMinutes(nvl(agg.getDeepMinutes()) + clamp0(deepMinutes));
        agg.setRemMinutes(nvl(agg.getRemMinutes()) + clamp0(remMinutes));
        agg.setLightMinutes(nvl(agg.getLightMinutes()) + clamp0(lightMinutes));
        agg.setAwakeMinutes(nvl(agg.getAwakeMinutes()) + clamp0(awakeMinutes));

        touch(agg);

        applySleepHighlights(user, agg);

        return dailyAggregateRepository.save(agg);
    }

    // ============================================================
    // Helpers: find/create + timezone
    // ============================================================

    private DailyAggregate findOrCreate(String userId, LocalDate date) {
        return dailyAggregateRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> DailyAggregate.builder()
                        .userId(userId)
                        .date(date)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());
    }

    private void touch(DailyAggregate agg) {
        if (agg.getCreatedAt() == null) agg.setCreatedAt(Instant.now());
        agg.setUpdatedAt(Instant.now());
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId).orElseThrow();
        // Nếu bạn có exception chuẩn hơn:
        // .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private ZoneId userZone(User user) {
        String tz = DEFAULT_TZ;
        try {
            if (user.getSettings() != null && user.getSettings().getTimezone() != null) {
                tz = user.getSettings().getTimezone();
            }
            return ZoneId.of(tz);
        } catch (Exception e) {
            return ZoneId.of(DEFAULT_TZ);
        }
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private int clamp0(int v) {
        return Math.max(0, v);
    }

    // ============================================================
    // Highlights / Summary: WATER
    // ============================================================

    private void applyWaterHighlights(User user, DailyAggregate agg) {
        int water = nvl(agg.getWaterMl());

        int goal = DEFAULT_WATER_GOAL_ML;
        if (user.getGoals() != null && user.getGoals().getDailyWaterMl() != null) {
            goal = user.getGoals().getDailyWaterMl();
        }

        List<String> highlights = safeCopy(agg.getHighlights());

        // Remove old water highlights to avoid duplicates
        highlights.removeIf(s -> s != null && s.startsWith("Water:"));

        if (water >= goal) {
            highlights.add("Water: Reached daily water goal ✅");
        } else {
            int remaining = Math.max(0, goal - water);
            highlights.add("Water: Need " + remaining + "ml to reach goal");
        }

        agg.setHighlights(highlights);

        // Summary: nếu summary đang null hoặc trước đó là water-only, ta có thể set/override nhẹ.
        // Nếu sau này bạn muốn summary tổng hợp cả steps/sleep/water, ta sẽ refactor thành builder riêng.
        if (water >= goal) {
            setOrBlendSummary(agg, "Hôm nay bạn đã uống đủ nước. Tốt lắm!");
        } else {
            int remaining = Math.max(0, goal - water);
            setOrBlendSummary(agg, "Bạn cần uống thêm khoảng " + remaining + "ml nước để đạt mục tiêu.");
        }
    }

    // ============================================================
    // Highlights / Summary: SLEEP
    // ============================================================

    private void applySleepHighlights(User user, DailyAggregate agg) {
        int sleep = nvl(agg.getSleepMinutes());

        // MVP: chưa có sleep goal trong UserGoals => dùng default 7h
        int goalMin = DEFAULT_SLEEP_GOAL_MIN;

        List<String> highlights = safeCopy(agg.getHighlights());
        highlights.removeIf(s -> s != null && s.startsWith("Sleep:"));

        if (sleep >= goalMin) {
            highlights.add("Sleep: Reached 7h+ ✅");
            setOrBlendSummary(agg, "Giấc ngủ hôm nay khá tốt. Tiếp tục duy trì nhé!");
        } else if (sleep >= 360) { // 6h
            highlights.add("Sleep: Slightly low (under 7h)");
            setOrBlendSummary(agg, "Bạn ngủ hơi ít. Nếu có thể, hãy cố gắng ngủ thêm để hồi phục tốt hơn.");
        } else {
            highlights.add("Sleep: Too low (under 6h) ⚠️");
            setOrBlendSummary(agg, "Bạn ngủ quá ít. Cố gắng ngủ đủ giấc để cải thiện sức khỏe và năng lượng.");
        }

        agg.setHighlights(highlights);
    }

    // ============================================================
    // Summary blending (MVP)
    // ============================================================

    /**
     * MVP: summary là 1 string, có thể bị cả water và sleep set.
     * Cách đơn giản: nếu summary trống -> set; nếu đã có -> giữ summary cũ (đỡ overwrite),
     * hoặc nối thêm câu nếu muốn.
     * <p>
     * Hiện tại mình chọn: nếu null/blank -> set, else -> giữ nguyên (an toàn).
     */
    private void setOrBlendSummary(DailyAggregate agg, String newSentence) {
        if (newSentence == null || newSentence.isBlank()) return;

        String current = agg.getSummary();
        if (current == null || current.isBlank()) {
            agg.setSummary(newSentence);
            return;
        }

        // Nếu bạn muốn nối thêm: uncomment dưới
        // if (!current.contains(newSentence)) agg.setSummary(current + " " + newSentence);
    }

    private List<String> safeCopy(List<String> src) {
        return src == null ? new ArrayList<>() : new ArrayList<>(src);
    }
}