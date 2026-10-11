package com.team.student_calendar.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 하루 호출 횟수를 제한하는 외부 API (일일 한도) */
@Getter
@AllArgsConstructor
public enum ApiUsageType {
    YES24(4000),
    NARU(400);

    private final int dailyLimit;
}
