package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.IsbnLookupError;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.entity.IsbnLookupEntity;

import java.time.LocalDateTime;

public record IsbnBookRes(
        String isbn,
        IsbnLookupStatus status,
        IsbnLookupError error,
        String title,
        String author,
        String publisher,
        String category,
        LocalDateTime registeredAt
) {

    public static IsbnBookRes from(IsbnLookupEntity lookup) {
        return new IsbnBookRes(lookup.getIsbn(), lookup.getStatus(), lookup.getError(), lookup.getTitle(),
                lookup.getAuthor(), lookup.getPublisher(), lookup.getCategory(), lookup.getRegisteredAt());
    }
}
