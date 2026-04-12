package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.constant.LevelRegexPattern;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.BookDTO;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import com.team.student_calendar.service.student.book.SelectReadBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

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
    public BookDTO[][] recommendBookToRead(Long studentId) {

        StudentEntity studentEntity = selectStudentService.findById(studentId);

        BookDTO[] toReadLiterature = getCurrentAndNextReadBook(studentEntity, BookCategory.LITERATURE);
        BookDTO[] toReadNonLiterature = getCurrentAndNextReadBook(studentEntity, BookCategory.NON_LITERATURE);

        return new BookDTO[][] {
                {
                        getPreviousReadBook(studentEntity, BookCategory.LITERATURE),
                        getSafe(toReadLiterature, 0),
                        getSafe(toReadLiterature, 1),
                },
                {
                        getPreviousReadBook(studentEntity, BookCategory.NON_LITERATURE),
                        getSafe(toReadNonLiterature, 0),
                        getSafe(toReadNonLiterature, 1),
                }
        };
    }


    /**
     * 인덱스 없을수도 있는 것을 안전하게 가져오기
     * @param books
     * @param index
     * @return BookDTO
     */
    private BookDTO getSafe(BookDTO[] books, int index) {

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
     * 마지막 수업에 읽었던 책
     */
    private BookDTO getPreviousReadBook(StudentEntity student, BookCategory category) {

        return selectReadBookService.findPreviousBookToRead(student.getId(), category);
    }


    /**
     * 오늘 수업에 읽어야 하는 책과 다음 수업에 읽어야 하는 책
     * 
     * @param student
     * @param category
     * @return BookDTO[]
     *
     * <p>
     *     [0] - 오늘 수업에 읽어야 하는 책<br/>
     *     [1] - 다음 수업에 읽어야 하는 책
     * </p>
     *
     */
    private BookDTO[] getCurrentAndNextReadBook(StudentEntity student, BookCategory category) {

        byte baseLevel = switch (category) {
            case LITERATURE -> BookLevelMapping
                    .customLevelOf(student.getFirstLevel());
            case NON_LITERATURE -> BookLevelMapping
                    .customLevelOf(convertNotLiteratureLevel(student.getFirstLevel()));
            default -> throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        };

        BookEntity[] books = selectBookService
                .findBookToReadByDifficultyAsc(student.getId(), baseLevel, category);
        if (books == null || books.length == 0) {
            return new BookDTO[0];
        }

        return Arrays.stream(books)
                .map(BookEntity::toDto)
                .toArray(BookDTO[]::new);
    }



    /**
     * 문학/비문학 레벨 선택 로직
     */
    private String convertNotLiteratureLevel(String firstLevel) {

        if (!LevelRegexPattern.LEVEL.matches(firstLevel)) {
            throw new BaseException(StudentErrorCode.INVALID_FIRST_LEVEL);
        }

        String[] parts = firstLevel.split("_");
        int number = Integer.parseInt(parts[1]);

        // 2B 이하 레벨
        if (number <= 2) {
            return "A_0";
        }

        // 3A 이상
        return "A_" + (number - 1);
    }
}
