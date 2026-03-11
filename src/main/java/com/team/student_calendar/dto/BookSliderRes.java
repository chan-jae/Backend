package com.team.student_calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookSliderRes {
    private Long bookNo;
    private String title;
    private String author;
    private String category;
    private String imageUrl;

    private boolean isRead;
}