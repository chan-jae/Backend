package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.CustomBookCreateReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertCustomBookService {

    private final BookRepository bookRepository;

    /**
     * 직접 등록 책 저장
     * @param req 제목, 레벨, 난이도, 카테고리
     * @return 저장된 BookEntity (type=CUSTOM)
     */
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookEntity create(CustomBookCreateReq req) {

        validateCategory(req.getCategory());

        LevelDifficultyRange range = LevelDifficultyRange.of(req.getLevel());
        range.validateDifficulty(req.getDifficulty());

        BookEntity saved = bookRepository.save(req.toEntity());
        log.info("custom book created — id: {}, title: {}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 카테고리 유효성 검사
     */
    private void validateCategory(String category) {
        try {
            BookCategory parsed = BookCategory.valueOf(category);
            if (parsed == BookCategory.ALL) {
                throw new BaseException(BookErrorCode.INVALID_CATEGORY);
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }
}
