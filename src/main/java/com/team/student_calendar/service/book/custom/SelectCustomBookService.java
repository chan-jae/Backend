package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectCustomBookService {

    private final BookRepository bookRepository;

    private static final Byte CUSTOM_TYPE = (byte) BookType.CUSTOM.getType();

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("category"),
            Sort.Order.asc("difficulty"),
            Sort.Order.asc("title"));

    /**
     * 직접 등록 책 전체 조회 (카테고리, 난이도순)
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAll() {
        return bookRepository.findAllByType(CUSTOM_TYPE, DEFAULT_SORT);
    }

    /**
     * 직접 등록 책 단건 조회 (custom 이 아니면 조회 실패)
     */
    @Transactional(readOnly = true)
    public BookEntity findById(Long id) {
        return bookRepository.findByIdAndType(id, CUSTOM_TYPE)
                .orElseThrow(() -> new BaseException(BookErrorCode.CUSTOM_BOOK_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<BookEntity> findForRecommend(String category, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        Sort sort = Sort.by(
                Sort.Order.asc("difficulty"),
                Sort.Order.asc("title"));

        return bookRepository.findByTypeAndCategory(CUSTOM_TYPE, category, PageRequest.of(0, limit, sort));
    }
}
