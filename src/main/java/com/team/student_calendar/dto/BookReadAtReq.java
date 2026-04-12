package com.team.student_calendar.dto;

import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Data
public class BookReadAtReq {

    private LocalDate readAt;
}
