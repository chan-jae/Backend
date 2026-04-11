package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.UpsertResult;
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

    private final BookJdbcRepository bookJdbcRepository;

    /**
     * 책 list 저장.
     * registered_at / updated_at 기반 SELECT 로 삽입/갱신/스킵 건수를 정확히 반환.
     *
     * @param bookList 책 list
     * @return 삽입·갱신·스킵 건수
     */
    @Transactional
    public UpsertResult saveBookList(List<BookCreateReq> bookList) {

        log.info("try to save {} books", bookList.size());

        UpsertResult result = bookJdbcRepository.bulkInsertBooks(bookList);

        log.info("book save complete — inserted: {}, updated: {}, skipped(no change): {}",
                result.insertedCount(), result.updatedCount(), result.skippedCount());

        return result;
    }
}
