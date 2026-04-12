package com.team.student_calendar.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecBookRes {

    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private String level;
    private Integer difficulty;
    private Byte cLevel;
    private Long bookNo;
    private String imageUrl;
    private Byte type;
    private LocalDateTime updatedAt;

    private Byte state;
    private LocalDate readAt;
}
