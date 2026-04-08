package com.team.student_calendar.service.book;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UpdateStudentBookService {
    private final StudentBookRepository studentBookRepository;

    @Transactional
    public void completeBook(Long studentId, Long bookId) {
        StudentBookEntity studentBook = studentBookRepository.findByStudentIdAndBookId(studentId, bookId)
                .orElseThrow(() -> new BaseException(BookErrorCode.BOOK_NOT_FOUND));

        studentBook.setState((byte) 1);

        studentBook.setReadAt(LocalDate.now());
    }
}
