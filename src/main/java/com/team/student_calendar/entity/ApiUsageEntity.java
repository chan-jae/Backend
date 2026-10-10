package com.team.student_calendar.entity;

import com.team.student_calendar.common.enums.ApiUsageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 외부 API 하루 호출 횟수 (API당 1행 미리 INSERT, 마지막 호출 날짜가 오늘이 아니면 다음 호출 때 1부터 다시 셈) */
@Getter
@Setter
@Entity
@Table(name = "api_usage", schema = "student_calendar")
public class ApiUsageEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "api", nullable = false, length = 20)
    private ApiUsageType api;

    @Column(name = "call_count", nullable = false)
    private int callCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
