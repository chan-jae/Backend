package com.team.student_calendar.service.book.custom;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CustomBookErrorCode;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
                .orElseThrow(() -> new BaseException(CustomBookErrorCode.CUSTOM_BOOK_NOT_FOUND));
    }

    /**
     * type=0 중 difficulty가 가장 가까운 책의 c_level을 그대로 차용
     */
    @Transactional(readOnly = true)
    public Byte resolveCLevel(String category, Integer difficulty) {
        return bookRepository.findNearestTeachingOceanBookByDifficulty(category, difficulty, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(BookEntity::getCLevel)
                .orElseThrow(() -> new BaseException(CustomBookErrorCode.LEVEL_RESOLUTION_FAILED));
    }
}
