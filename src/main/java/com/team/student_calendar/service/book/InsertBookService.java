package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.constant.LevelRegexPattern;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookRepository bookRepository;
    private final BookJdbcRepository bookJdbcRepository;

    /**
     * 책 list 저장
     * @param bookList 책 list
     * @return 삽입,갱신,스킵 건수
     */
    @Transactional
    public UpsertResult saveBookList(List<BookCreateReq> bookList) {

        log.info("try to save {} books", bookList.size());

        UpsertResult result = bookJdbcRepository.bulkInsertBooks(bookList);

        log.info("book save complete — inserted: {}, updated: {}, skipped(no change): {}",
                result.insertedCount(), result.updatedCount(), result.skippedCount());

        return result;
    }


    /**
     * 책 1권 수동 등록
     * @param req 수동 책 등록 필수 필드
     * @return BookEntity
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public BookEntity saveBook(ManualBookDto req) {

        log.info("try to save [{}] book", req.getTitle());

        // 카테고리, 레벨, 난이도, 타입 검증
        req.validate();

        BookEntity bookEntity = BookEntity.builder()
                .title(req.getTitle())
                .author(req.getAuthor())
                .publisher(req.getPublisher())
                .category(req.getCategory())
                .level(req.getLevel())
                .difficulty(req.getDifficulty())
                .cLevel(BookLevelMapping.customLevelOf(req.getLevel()))
                .type(BookType.of(req.getType()).getType())
                .updatedAt(LocalDateTime.now())
                .build();

        bookRepository.save(bookEntity);

        log.info("book save complete - [{}]", req.getTitle());

        return bookEntity;
    }
}
