package com.team.student_calendar.common.dto;

import com.team.student_calendar.entity.BookEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.awt.print.Book;

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

    private String level;

    private Integer difficulty;

    private Integer bookNo;


    public BookEntity toEntity () {
        return BookEntity.builder()
                .title(this.title)
                .author(this.author)
                .publisher(this.publisher)
                .category(this.category)
                .level(this.level)
                .difficulty(this.difficulty)
                .bookNo(this.bookNo)
                .build();
    }
}
