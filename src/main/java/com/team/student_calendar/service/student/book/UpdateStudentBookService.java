package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.enums.StudentBookState;
import com.team.student_calendar.dto.ReadBookUpdateReq;
import com.team.student_calendar.entity.StudentBookEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateStudentBookService {

    private final SelectReadBookService selectReadBookService;


    /**
     * 읽은 책 상태 변경
     * @param studentId 학생 pk
     * @param bookId 책 pk
     * @param req 상태 변경 요청 정보(state, readAt)
     */
    @Transactional
    public void updateReadBook(Long studentId, Long bookId, ReadBookUpdateReq req) {

        log.info("try to update read book");

        /* 상태 업데이트*/
        if (req.getState() != null) {
            changeState(studentId, bookId, req);
        }

        /* 읽은 날짜 업데이트*/
        changeReadAt(studentId, bookId, req);

        log.info("complete update read book");
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
