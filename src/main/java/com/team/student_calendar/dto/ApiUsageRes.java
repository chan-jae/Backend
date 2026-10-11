package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.ApiUsageType;
import com.team.student_calendar.entity.ApiUsageEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부 API 오늘 사용량
 * @param usedCount 오늘 호출 횟수 (마지막 호출이 오늘 이전이면 0)
 * @param remainingCount 오늘 남은 횟수
 */
public record ApiUsageRes(ApiUsageType api, int dailyLimit, int usedCount, int remainingCount, LocalDateTime updatedAt) {

    public static ApiUsageRes from(ApiUsageEntity entity, LocalDate today) {
        int limit = entity.getApi().getDailyLimit();
        // DB 값은 다음 호출 때 초기화되므로, 날짜가 바뀐 뒤 아직 호출이 없으면 여기서 0으로 계산
        int used = entity.getUpdatedAt().toLocalDate().equals(today) ? entity.getCallCount() : 0;
        return new ApiUsageRes(entity.getApi(), limit, used, Math.max(0, limit - used), entity.getUpdatedAt());
    }
}
