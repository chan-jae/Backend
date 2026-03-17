package com.team.student_calendar.service.book;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectBookService {

    private final BookRepository bookRepository;


    /**
     * List안에 있는 bookNo를 가지는 BookEntity 가져오기
     * */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByBookNoList(List<Long> bookNoList) {

        return bookRepository.findAllByBookNoIn(bookNoList);
    }


    /**
     * 난이도, 제목순으로 BookEntity 가져오기
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByDifficultyAscAndTitleAsc() {

        Sort sort = Sort.by(
                Sort.Order.asc("difficulty"),
                Sort.Order.asc("title")
        );

        return bookRepository.findAll(sort);
    }


    @Transactional(readOnly = true)
    public BookEntity findById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() -> new BaseException(BookErrorCode.BOOK_NOT_FOUND));
    }
}
