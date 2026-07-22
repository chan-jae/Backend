package com.team.student_calendar.service.book.custom;

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
    private final SelectCustomBookService selectCustomBookService;

    /**
     * 직접 등록 책 저장
     * @param req 제목, 난이도, 카테고리
     * @return 저장된 BookEntity (type=CUSTOM)
     */
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookEntity create(CustomBookCreateReq req) {

        validateCategory(req.getCategory());

        BookEntity entity = req.toEntity();
        entity.setCLevel(selectCustomBookService.resolveCLevel(req.getCategory(), req.getDifficulty()));

        BookEntity saved = bookRepository.save(entity);
        log.info("custom book created — id: {}, title: {}", saved.getId(), saved.getTitle());
        return saved;
    }

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
