package com.team.student_calendar.service.book;

import com.team.student_calendar.common.enums.StudentBookState;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import com.team.student_calendar.dto.ReadBookUpdateReq;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.service.student.book.SelectReadBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateStudentBookService {

    private final SelectReadBookService selectReadBookService;



    @Transactional
    public void updateReadBook(Long studentId, Long bookId, ReadBookUpdateReq req) {

        /* 상태 업데이트*/
        if (req.getState() != null) {
            changeState(studentId, bookId, req);
        }

        /* 읽은 날짜 업데이트*/
        changeReadAt(studentId, bookId, req);
    }


    private void changeState(Long studentId, Long bookId, ReadBookUpdateReq req) {

        StudentBookEntity studentBook = selectReadBookService.findByStudentAndBook(studentId, bookId);

        Byte stateFromString = StudentBookState.getStateFromString(req.getState());
        studentBook.setState(stateFromString);
    }


    private void changeReadAt(Long studentId, Long bookId, ReadBookUpdateReq req) {

        StudentBookEntity studentBook = selectReadBookService.findByStudentAndBook(studentId, bookId);

        studentBook.setReadAt(req.getReadAt());
    }
}
