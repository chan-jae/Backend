package com.team.student_calendar.service.book;

import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
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
    public List<BookEntity> findAllByBookNoList(List<Integer> bookNoList) {

        return bookRepository.findAllByBookNoIn(bookNoList);
    }
}
