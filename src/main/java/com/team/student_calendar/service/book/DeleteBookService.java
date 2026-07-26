package com.team.student_calendar.service.book;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeleteBookService {

    private final BookRepository bookRepository;
    private final SelectBookService selectBookService;




    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void deleteBook(Long id) {

        log.info("try to delete book: {}", id);

        BookEntity bookEntity = selectBookService.findById(id);

        if (BookType.CUSTOM.getType() != bookEntity.getType()) {
            throw new BaseException(BookErrorCode.INVALID_TYPE, "수정 가능한 책 타입이 아닙니다.");
        }
        log.info("check custom book complete");

        bookRepository.delete(bookEntity);

        log.info("book delete complete - book: {}", id);
    }
}
