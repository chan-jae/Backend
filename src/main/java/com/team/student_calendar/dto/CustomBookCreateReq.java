package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.entity.BookEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomBookCreateReq {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotNull(message = "난이도는 필수 항목입니다.")
    @Positive(message = "난이도는 0 이상의 정수여야 합니다.")
    private Integer difficulty;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    public BookEntity toEntity() {
        return BookEntity.builder()
                .title(this.title)
                .category(this.category)
                .difficulty(this.difficulty)
                .type((byte) BookType.CUSTOM.getType())
                .author("자체 등록")
                .publisher("용천점")
                .state((byte) 0)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
