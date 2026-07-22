package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.CustomBookCreateReq;
import com.team.student_calendar.entity.BookEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class    UpdateCustomBookService {

    private final SelectCustomBookService selectCustomBookService;

    /**
     * 직접 등록 책 수정
     */
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookEntity update(Long id, CustomBookCreateReq req) {

        validateCategory(req.getCategory());

        BookEntity entity = selectCustomBookService.findById(id);
        entity.setTitle(req.getTitle());
        entity.setDifficulty(req.getDifficulty());
        entity.setCategory(req.getCategory());
        entity.setCLevel(selectCustomBookService.resolveCLevel(req.getCategory(), req.getDifficulty()));
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
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
