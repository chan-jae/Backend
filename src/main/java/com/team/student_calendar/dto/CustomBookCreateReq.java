package com.team.student_calendar.dto;

import com.team.student_calendar.entity.CustomBookEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CustomBookCreateReq {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotNull(message = "난이도는 필수 항목입니다.")
    @Positive(message = "난이도는 0 이상의 정수여야 합니다.")
    private Integer difficulty;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    public CustomBookEntity toEntity() {
        return CustomBookEntity.builder()
                .title(this.title)
                .difficulty(this.difficulty)
                .category(this.category)
                .build();
    }
}
