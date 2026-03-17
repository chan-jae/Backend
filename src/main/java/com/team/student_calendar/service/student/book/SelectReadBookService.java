package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.ReadBooksRes;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SelectReadBookService {

    private final StudentBookRepository studentBookRepository;
    private final SelectStudentService selectStudentService;



    /**
     * 읽지 않은 책이 맞는지
     */
    @Transactional(readOnly = true)
    public boolean isReadBook(
            Long studentId,
            Long bookId
    ) {

        Optional<StudentBookEntity> optional = studentBookRepository.findByStudentIdAndBookId(studentId, bookId);
        return optional.isPresent();
    }


    /**
     * 학생이 읽은 책 가져오기
     * @param studentId 학생 pk
     * @return ReadBooksRes
     */
    @Transactional(readOnly = true)
    public ReadBooksRes findReadBooksByStudentId(Long studentId, String category) {

        /* 학생 찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 대문자로 변환*/
        category = category.toUpperCase();

        /* 카테고리 분류가 유효한지*/
        BookCategory bookCategory;
        try {
            bookCategory = BookCategory.valueOf(category);
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }

        /* 카테고리별로 분기*/
        List<StudentBookEntity> readBookList = switch (bookCategory) {
            // 읽은 책 목록 모두 가져오기
            case ALL -> studentBookRepository
                    .findByStudentId(studentId);
            // 문학만 가져오기
            case LITERATURE -> studentBookRepository
                    .findByStudentIdAndBook_Category(studentId, BookCategory.LITERATURE.name());
            // 비문학만 가져오기
            case NON_LITERATURE -> studentBookRepository
                    .findByStudentIdAndBook_CategoryNot(studentId, BookCategory.LITERATURE.name());
        };

        /* 응답 데이터 만들기*/
        List<ReadBooksRes.Book> books = readBookList.stream()
                .map(StudentBookEntity::toReadBooksRes)
                .toList();

        ReadBooksRes res = new ReadBooksRes();
        res.setStudentId(studentId);
        res.setBooks(books);

        return res;
    }
}
