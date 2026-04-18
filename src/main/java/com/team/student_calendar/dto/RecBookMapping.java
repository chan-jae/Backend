package com.team.student_calendar.dto;

import com.team.student_calendar.entity.BookEntity;

import java.time.LocalDate;

public interface RecBookMapping {

    BookEntity getBook();
    Byte getState();
    LocalDate getReadAt();
}
