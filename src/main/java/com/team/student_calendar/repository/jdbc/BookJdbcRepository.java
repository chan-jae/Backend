package com.team.student_calendar.repository.jdbc;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.UpsertResult;

import java.util.List;

public interface BookJdbcRepository {

    int BATCH_SIZE = 150;

    /**
     * 책 목록을 배치로 UPSERT 실행.
     * registered_at / updated_at 컬럼 기반 SELECT로 삽입/갱신/스킵 건수를 반환.
     */
    UpsertResult bulkInsertBooks(List<BookCreateReq> bookList);
}
