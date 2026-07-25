package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectCustomBookService {

    private final BookRepository bookRepository;

    private static final Byte CUSTOM_TYPE = (byte) BookType.CUSTOM.getType();

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("difficulty"),
            Sort.Order.asc("title"));

    /**
     * 직접 등록 책 전체 조회 (카테고리, 난이도순)
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAll() {
        return bookRepository.findAllByType(CUSTOM_TYPE, DEFAULT_SORT);
    }
}
