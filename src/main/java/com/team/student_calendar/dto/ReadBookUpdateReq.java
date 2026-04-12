package com.team.student_calendar.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReadBookUpdateReq {

    private String state;
    private LocalDate readAt;
}
