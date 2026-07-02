package com.team.student_calendar.service.student.book;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.RecBookRes;
import com.team.student_calendar.dto.ReadBooksRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param category 카테고리
     * @param title 검색할 책 제목 (없으면 전체)
     * @param pageable 페이지 옵션
     * @return ReadBooksRes
     */
    @Transactional(readOnly = true)
    public ReadBooksRes findReadBooksByStudentId(
            Long studentId,
            String category,
            String title,
            Pageable pageable
    ) {

        /* 학생 찾기*/
        boolean isExistStudent = selectStudentService.existsById(studentId);
        if (!isExistStudent) throw new BaseException(StudentErrorCode.STUDENT_NOT_FOUND);

        /* 기본 정렬 지정*/
        if (pageable.getSort().isUnsorted()) {
            Sort defaultSort = Sort.by(
                    Sort.Order.asc("state"),
                    Sort.Order.desc("readAt").nullsLast(),
                    Sort.Order.desc("registeredAt")
            );
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
        }

        /* 대문자로 변환*/
        category = category.toUpperCase();

        /* 카테고리 분류가 유효한지*/
        BookCategory bookCategory;
        try {
            bookCategory = BookCategory.valueOf(category);
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }

        boolean hasTitle = title != null && !title.isBlank();

        /* 카테고리별로 분기*/
        Slice<StudentBookEntity> readBookList = switch (bookCategory) {
            // 읽은 책 목록 모두 가져오기 (제목 검색 포함)
            case ALL -> hasTitle
                    ? studentBookRepository.findByStudentIdAndBook_TitleContainingIgnoreCase(studentId, title, pageable)
                    : studentBookRepository.findByStudentId(studentId, pageable);
            // 문학만 가져오기 (제목 검색 포함)
            case LITERATURE -> hasTitle
                    ? studentBookRepository.findByStudentIdAndBook_CategoryAndBook_TitleContainingIgnoreCase(
                            studentId, BookCategory.LITERATURE.name(), title, pageable)
                    : studentBookRepository.findByStudentIdAndBook_Category(studentId, BookCategory.LITERATURE.name(), pageable);
            // 비문학만 가져오기 (제목 검색 포함)
            case NON_LITERATURE -> hasTitle
                    ? studentBookRepository.findByStudentIdAndBook_CategoryNotAndBook_TitleContainingIgnoreCase(
                            studentId, BookCategory.LITERATURE.name(), title, pageable)
                    : studentBookRepository.findByStudentIdAndBook_CategoryNot(studentId, BookCategory.LITERATURE.name(), pageable);
        };

        /* 필요한 필드 추가*/
        Slice<ReadBooksRes.Book> books = readBookList
                .map(StudentBookEntity::toReadBooksRes);

        return ReadBooksRes.of(studentId, books);
    }


    public StudentBookEntity findByStudentAndBook(Long studentId, Long bookId) {

        return studentBookRepository.findByStudentIdAndBookId(studentId, bookId)
                .orElseThrow(() -> new BaseException(ReadBookErrorCode.READ_BOOK_NOT_FOUND));
    }


    /**
     * 가장 최근에 읽었었던 책 가져오기
     * @param studentId 학생 pk
     * @param category 카테고리
     * @return 책
     */
    public RecBookRes findPreviousBookToRead(Long studentId, BookCategory category) {

        /* 읽은순, 등록순으로 가져오기*/
        Sort sort = Sort.by(
                Sort.Order.desc("readAt").nullsLast(),
                Sort.Order.desc("registeredAt")
        );

        /* 최근에 읽었던 책 가져오기*/
        StudentBookEntity studentBookEntity = switch (category) {
            case LITERATURE -> studentBookRepository
                    .findTopByStudent_IdAndBook_Category(studentId, category.name(), sort)
                    .orElse(null);
            case NON_LITERATURE -> studentBookRepository
                    .findTopByStudent_IdAndBook_CategoryNot(studentId, BookCategory.LITERATURE.name(), sort)
                    .orElse(null);
            default -> null;
        };
        if (studentBookEntity == null) {
            return null;
        }

        BookEntity book = studentBookEntity.getBook();

        /* entity를 RecBookRes로 변환*/
        return book.toRecBook(studentBookEntity.getState(), studentBookEntity.getReadAt());
    }
}
