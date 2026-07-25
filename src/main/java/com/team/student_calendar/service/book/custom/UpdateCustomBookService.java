package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.dto.CustomBookCreateReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.service.book.SelectBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateCustomBookService {

    private final SelectBookService selectBookService;

    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookEntity update(Long id, CustomBookCreateReq req) {

        BookCategory.validate(req.getCategory());
        LevelDifficultyRange.validate(req.getLevel(), req.getDifficulty());

        BookEntity entity = selectBookService.findById(id);
        entity.setTitle(req.getTitle());
        entity.setDifficulty(req.getDifficulty());
        entity.setCategory(req.getCategory());
        entity.setLevel(req.getLevel());
        entity.setCLevel(BookLevelMapping.customLevelOf(req.getLevel()));
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
