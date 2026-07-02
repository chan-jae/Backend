package com.team.student_calendar.dto;

import com.team.student_calendar.entity.CustomBookEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomBookRes {

    private Long id;
    private String title;
    private Integer difficulty;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomBookRes from(CustomBookEntity entity) {
        return CustomBookRes.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .difficulty(entity.getDifficulty())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
