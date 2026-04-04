package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.BookListCreateRes;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final SelectBookService selectBookService;
    private final BookJdbcRepository bookJdbcRepository;


    /**
     * 책 list 저장
     * @param bookList 책 list
     * @return 실제로 삽입된 데이터 개수
     */
    @Transactional
    public BookListCreateRes saveBookList(List<BookCreateReq> bookList) {

        /* 책 리스트 비어있는지 체크*/
        if (bookList.isEmpty()) {
            log.warn("book list is empty");
            return new BookListCreateRes(0);
        }

        /*
            ISNERT IGNORE 때문에 성공했지만 정확히 알 수 없어서 -2를 반환.
            나누기 -2를 하면 영향을 받은 행 개수를 알 수 있다.
        */
        long existingBookCnt = selectBookService.countAll();
        log.info("try {} books batch insert", bookList.size());
        int result = bookJdbcRepository.bulkInsertBooks(bookList);
        long insertedCnt;
        if (result == 0) {
            insertedCnt = 0;
        }
        else {
            insertedCnt = (result / -2) - existingBookCnt;
        }

        log.info("{} books batch insert success", insertedCnt);

        return new BookListCreateRes(insertedCnt);
    }
}
