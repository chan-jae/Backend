package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
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

    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookEntity create(CustomBookCreateReq req) {

        BookCategory.validate(req.getCategory());
        LevelDifficultyRange.validate(req.getLevel(), req.getDifficulty());

        BookEntity saved = bookRepository.save(req.toEntity());
        log.info("custom book created — id: {}, title: {}", saved.getId(), saved.getTitle());
        return saved;
    }
}
