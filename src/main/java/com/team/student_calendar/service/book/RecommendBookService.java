package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.constant.LevelRegexPattern;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.RecBookRes;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.service.student.SelectStudentService;
import com.team.student_calendar.service.student.book.SelectReadBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBookService {

    private final SelectBookService selectBookService;
    private final SelectStudentService selectStudentService;
    private final SelectReadBookService selectReadBookService;


    /**
     * 책 추천 기능 (이전, 현재, 다음)
     */
    @Transactional(readOnly = true)
    public RecBookRes[][] recommendBookToRead(Long studentId) {

        StudentEntity studentEntity = selectStudentService.findById(studentId);

        if (studentEntity.getFirstLevel() == null) {
            return null;
        }

        /* 읽어야 하는 문학 책 가져오기*/
        RecBookRes[] toReadLiterature = getCurrentAndNextReadBook(studentEntity, BookCategory.LITERATURE);
        /* 읽어야 하는 비문학 책 가져오기*/
        RecBookRes[] toReadNonLiterature = getCurrentAndNextReadBook(studentEntity, BookCategory.NON_LITERATURE);
        /* 읽어야 하는 마이북 가져오기 (난이도 분류 없음) */
        RecBookRes[] toReadCustom = selectBookService.findCustomBookToRead(studentEntity.getId());

        return new RecBookRes[][] {
                {
                        getPreviousReadBook(studentEntity, BookCategory.LITERATURE),
                        getSafe(toReadLiterature, 0),
                        getSafe(toReadLiterature, 1),
                },
                {
                        getPreviousReadBook(studentEntity, BookCategory.NON_LITERATURE),
                        getSafe(toReadNonLiterature, 0),
                        getSafe(toReadNonLiterature, 1),
                },
                {
                        getPreviousReadCustomBook(studentEntity),
                        getSafe(toReadCustom, 0),
                        getSafe(toReadCustom, 1),
                }
        };
    }


    /**
     * 인덱스로 참조 안전하게 하기
     * @param books 책
     * @param index 인덱스
     * @return BookDTO
     */
    private RecBookRes getSafe(RecBookRes[] books, int index) {

        if (books == null || books.length == 0) {
            return null;
        }

        try {
            return books[index];
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 마지막에 읽었던 책
     * @param student 학생
     * @param category 카테고리
     */
    private RecBookRes getPreviousReadBook(StudentEntity student, BookCategory category) {

        return selectReadBookService.findPreviousBookToRead(student.getId(), category);
    }


    /**
     * 마지막으로 읽었던 마이북 (책 추가 시 타입을 구분하지 않으므로 category 대신 book.type으로 조회)
     * @param student 학생
     */
    private RecBookRes getPreviousReadCustomBook(StudentEntity student) {

        return selectReadBookService.findPreviousCustomBookToRead(student.getId());
    }


    /**
     * 오늘 수업에 읽어야 하는 책과 다음 수업에 읽어야 하는 책
     * @param student 학생
     * @param category 카테고리
     * @return 책 array
     *
     * <p>
     *     [0] - 오늘 수업에 읽어야 하는 책<br/>
     *     [1] - 다음 수업에 읽어야 하는 책
     * </p>
     *
     */
    private RecBookRes[] getCurrentAndNextReadBook(StudentEntity student, BookCategory category) {

        byte baseLevel = switch (category) {
            case LITERATURE -> BookLevelMapping
                    .customLevelOf(student.getFirstLevel());
            case NON_LITERATURE -> BookLevelMapping
                    .customLevelOf(convertNotLiteratureLevel(student.getFirstLevel()));
            default -> throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        };
        
        RecBookRes[] books = switch (category) {
            case LITERATURE -> selectBookService
                    .findLiteratureBookToRead(student.getId(), baseLevel);
            case NON_LITERATURE -> selectBookService
                    .findNonLiteratureBookToRead(student.getId(), baseLevel);
            default -> null;
        };

        if (books == null || books.length == 0) {
            return new RecBookRes[0];
        }

        return books;
    }



    /**
     * 비문학 레벨 변환
     * @param baseLevel 기준 레벨
     * @return 비문학 레벨
     */
    private String convertNotLiteratureLevel(String baseLevel) {

        if (!LevelRegexPattern.LEVEL.matches(baseLevel)) {
            throw new BaseException(StudentErrorCode.INVALID_FIRST_LEVEL);
        }

        String[] parts = baseLevel.split("_");
        int number = Integer.parseInt(parts[1]);

        if (number == 0) return parts[0] + "_" + number;
        return parts[0] + "_" + (number - 1);
    }
}
