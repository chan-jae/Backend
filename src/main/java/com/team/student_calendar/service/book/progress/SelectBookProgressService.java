package com.team.student_calendar.service.book.progress;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookProgressErrorCode;
import com.team.student_calendar.entity.BookProgressEntity;
import com.team.student_calendar.repository.BookProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelectBookProgressService {

    private final BookProgressRepository bookProgressRepository;


    @Transactional(readOnly = true)
    public BookProgressEntity findByStudentId(Long studentId) {

        return bookProgressRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BaseException(BookProgressErrorCode.BOOK_PROGRESS_NOT_FOUND));
    }
}
