package com.team.student_calendar.service.book;

import com.team.student_calendar.common.enums.StudentBookState;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.BookStateReq;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.book.SelectReadBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UpdateStudentBookService {

    private final SelectReadBookService selectReadBookService;



    @Transactional
    public void changeState(Long studentId, Long bookId, BookStateReq req) {

        StudentBookEntity studentBook = selectReadBookService.findByStudentAndBook(studentId, bookId);

        Byte stateFromString = StudentBookState.getStateFromString(req.getState());
        studentBook.setState(stateFromString);
    }
}
