package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.service.book.SelectBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCustomBookService {

    private final BookRepository bookRepository;
    private final SelectBookService selectBookService;

    /**
     * 직접 등록 책 삭제
     */
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public void delete(Long id) {
        BookEntity entity = selectBookService.findById(id);
        bookRepository.delete(entity);
        log.info("custom book deleted — id: {}", id);
    }
}
