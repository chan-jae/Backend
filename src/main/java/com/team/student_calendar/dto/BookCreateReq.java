package com.team.student_calendar.dto;

import com.team.student_calendar.entity.BookEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCreateReq {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotBlank(message = "저자는 필수 항목입니다.")
    private String author;

    @NotBlank(message = "출판사는 필수 항목입니다.")
    private String publisher;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    @NotBlank(message = "레벨은 필수 항목입니다.")
    private String level;

    @NotNull(message = "난이도 필수 항목입니다.")
    private Integer difficulty;

    @NotNull(message = "책넘버는 필수 항목입니다.")
    private Long bookNo;

    @NotBlank(message = "이미지주소는 필수 항목입니다.")
    private String imageUrl;

    private Byte type;

    public BookEntity toEntity() {
        return BookEntity.builder()
                .title(this.title)
                .author(this.author)
                .publisher(this.publisher)
                .category(this.category)
                .level(this.level)
                .difficulty(this.difficulty)
                .bookNo(this.bookNo)
                .imageUrl(this.imageUrl)
                .type(this.type)
                .build();
    }
}
