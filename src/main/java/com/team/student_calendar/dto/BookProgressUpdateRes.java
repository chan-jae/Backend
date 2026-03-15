package com.team.student_calendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.student_calendar.entity.BookEntity;
import lombok.Data;

@Data
public class BookProgressUpdateRes {

    private Long id;

    private String title;

    private String author;

    private String publisher;

    private String category;

    private String level;

    private Integer difficulty;

    private Long bookNo;

    private String imageUrl;

    private boolean firstBook; // 제일 처음 책인지

    private boolean lastBook; // 마지막 단계의 책인지


    public BookProgressUpdateRes() { }

    public BookProgressUpdateRes(BookEntity book, boolean isFirst, boolean isLast) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.publisher = book.getPublisher();
        this.category = book.getCategory();
        this.level = book.getLevel();
        this.difficulty = book.getDifficulty();
        this.bookNo = book.getBookNo();
        this.imageUrl = book.getImageUrl();
        this.firstBook = isFirst;
        this.lastBook = isLast;
    }

}
