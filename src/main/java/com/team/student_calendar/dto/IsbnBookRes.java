package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.IsbnLookupError;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.entity.IsbnLookupEntity;

import java.time.LocalDateTime;

public record IsbnBookRes(
        Long lookupId,
        Long bookId,
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
        Long bookId = lookup.getBook() == null ? null : lookup.getBook().getId();
        return new IsbnBookRes(lookup.getId(), bookId, lookup.getIsbn(), lookup.getStatus(), lookup.getError(), lookup.getTitle(),
                lookup.getAuthor(), lookup.getPublisher(), lookup.getCategory(), lookup.getRegisteredAt());
    }
}
