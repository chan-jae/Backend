package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.enums.StudentBookState;
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
    public void updateBookState(Long studentId, Long bookId, String stateStr) {
        StudentBookEntity studentBook = studentBookRepository.findByStudentIdAndBookId(studentId, bookId)
                .orElseThrow(() -> new BaseException(BookErrorCode.BOOK_NOT_FOUND));

        Byte newState = StudentBookState.getStateFromString(stateStr);

        studentBook.setState(newState);

        // done이면 시간 기록
        if (newState == (byte) StudentBookState.DONE.getState()) {
            studentBook.setReadAt(LocalDate.now());
        }
    }
}
