package com.team.student_calendar.dto;

import com.team.student_calendar.entity.BookEntity;
import lombok.Data;

import java.util.List;

@Data
public class ReadBooksRes {

    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private String level;
    private Integer difficulty;
    private Long bookNo;
    private String imageUrl;
    private Byte type;
    private Byte state;
}
