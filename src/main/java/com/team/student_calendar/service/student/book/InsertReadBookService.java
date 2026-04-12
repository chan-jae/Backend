package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.enums.StudentBookState;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import com.team.student_calendar.dto.ReadBooksSaveReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.book.SelectBookService;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertReadBookService {

    private final StudentBookRepository studentBookRepository;
    private final SelectReadBookService selectReadBookService;
    private final SelectStudentService selectStudentService;
    private final SelectBookService selectBookService;



    @Transactional
    public StudentBookEntity saveReadBookList(ReadBooksSaveReq req) {

        log.info("try to save student: {}, read book: {}, state: {}",
                req.getStudentId(), req.getBookId(), req.getStateStr());

        // 학생 있는지 체크
        StudentEntity student = selectStudentService.findById(req.getStudentId());

        // 책 있는지 체크
        BookEntity book = selectBookService.findById(req.getBookId());

        // 상태 있는지 체크
        Byte state = StudentBookState.getStateFromString(req.getStateStr());
        System.out.println("state: " + state);
        // 이미 읽은책이 있는지 체크
        boolean isRead = selectReadBookService
                .isReadBook(req.getStudentId(), req.getBookId());
        if (isRead) {
            throw new BaseException(ReadBookErrorCode.ALREADY_READ_BOOK);
        }

        // 엔티티 생성
        StudentBookEntity entity = StudentBookEntity.builder()
                .student(student)
                .book(book)
                .state(state)
                .build();

        // 학생 읽은 책 저장하기
        StudentBookEntity saved = studentBookRepository.save(entity);

        log.info("success to save student read book");

        return saved;
    }
}
