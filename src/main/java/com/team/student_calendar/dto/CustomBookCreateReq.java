package com.team.student_calendar.dto;

import com.team.student_calendar.common.constant.BookLevelMapping;
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

    @NotBlank(message = "저자는 필수 항목입니다.")
    private String author;

    @NotBlank(message = "출판사는 필수 항목입니다.")
    private String publisher;

    @NotBlank(message = "레벨은 필수 항목입니다.")
    private String level;

    @NotNull(message = "난이도는 필수 항목입니다.")
    @Positive(message = "난이도는 0 이상의 정수여야 합니다.")
    private Integer difficulty;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    public BookEntity toEntity() {
        return BookEntity.builder()
                .title(this.title)
                .author(this.author)
                .publisher(this.publisher)
                .category(this.category)
                .level(this.level)
                .difficulty(this.difficulty)
                .cLevel(BookLevelMapping.customLevelOf(this.level))
                .type((byte) BookType.CUSTOM.getType())
                .state((byte) 0)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
