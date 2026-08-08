package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.entity.BookEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateBookService {

    private final SelectBookService selectBookService;
    private final ValidateBookDupService validateBookDupService;




    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public BookEntity updateBook(Long id, ManualBookDto req) {

        log.info("try to update book: {}", id);

        // 필드 검증
        req.validate();

        // 등록된 책 있는지 체크
        validateBookDupService.checkBookDuplication(req.getTitle(), req.getAuthor());

        BookEntity entity = selectBookService.findById(id);

        // 커스텀 책만 수정가능
        if (BookType.CUSTOM.getType() != entity.getType()) {
            throw new BaseException(BookErrorCode.INVALID_TYPE, "수정 가능한 책 타입이 아닙니다.");
        }

        byte isActive = (byte) (Boolean.parseBoolean(req.getIsActive()) ? 1 : 0);

        entity.setTitle(req.getTitle());
        entity.setAuthor(req.getAuthor());
        entity.setPublisher(req.getPublisher());
        entity.setDifficulty(req.getDifficulty());
        entity.setCategory(req.getCategory());
        entity.setLevel(req.getLevel());
        entity.setCLevel(BookLevelMapping.customLevelOf(req.getLevel()));
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setIsActive(isActive);
        entity.setBHash(BookHashUtil.generateBookHashKey(req.getTitle(), req.getAuthor()));
        entity.setUpdatedAt(LocalDateTime.now());

        log.info("book update complete - book: {}", id);

        return entity;
    }
}
