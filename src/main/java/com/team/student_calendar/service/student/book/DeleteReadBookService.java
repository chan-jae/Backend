package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import com.team.student_calendar.entity.BookEntity;
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
public class DeleteReadBookService {

    private final StudentBookRepository studentBookRepository;
    private final SelectStudentService selectStudentService;
    private final SelectBookService selectBookService;


    /**
     * 학생이 읽었던 책 모두 지우기
     * @param studentId 학생 id
     */
    @Transactional
    public void deleteReadBooks(Long studentId) {

        log.info("try to delete student: {} read all books", studentId);

        /* 학생 찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 학생이 읽은 책 모두 지우기*/
        long deleted = studentBookRepository.deleteAllByStudent(student);

        log.info("success to delete {} books", deleted);
    }


    /**
     * 학생이 읽었던 책 1권 지우기
     * @param studentId 학생 pk
     * @param bookId 책 pk
     */
    @Transactional
    public void deleteReadBook(Long studentId, Long bookId) {

        log.info("try to delete student: {} read 1 book: {}", studentId, bookId);

        /* 학생찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 책 찾기*/
        BookEntity book = selectBookService.findById(bookId);

        /* 책 1개만 지우기*/
        long deleted = studentBookRepository.deleteByStudentAndBook(student, book);
        // 삭제된게 없으면 throw
        if (deleted == 0) {
            throw new BaseException(ReadBookErrorCode.READ_BOOK_NOT_FOUND);
        }

        log.info("success to delete read 1 book");
    }
}
