package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookJdbcRepository bookJdbcRepository;

    @Transactional
    public void saveBookList(List<BookCreateReq> bookList) {
        if (bookList.isEmpty()) {
            return;
        }
        bookJdbcRepository.bulkInsertBooks(bookList);
    }
}
