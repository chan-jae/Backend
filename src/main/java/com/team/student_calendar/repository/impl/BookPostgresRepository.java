package com.team.student_calendar.repository.impl;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Profile("postgres")
@Repository("bookPostgresRepository")
@RequiredArgsConstructor
public class BookPostgresRepository implements BookJdbcRepository {

    // registered_at 은 DB 함수 now() 로 세팅 — 파라미터(?) 수는 8개 그대로 유지
    private static final String INSERT_SQL = """
            INSERT INTO student_calendar.book
            (title, author, publisher, category, "level", difficulty, book_no, image_url,
             registered_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())
            ON CONFLICT (book_no) DO NOTHING
            """;

    // registered_at >= batchTime 이면 이번 배치에서 새로 INSERT 된 행
    private static final String COUNT_INSERTED_SQL = """
            SELECT COUNT(*)
            FROM student_calendar.book
            WHERE registered_at >= ?
            """;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public UpsertResult bulkInsertBooks(List<BookCreateReq> bookList) {

        int size = bookList.size();
        if (size == 0) {
            return new UpsertResult(0, 0, 0, null);
        }

        // 배치 시작 직전 DB 서버 시각 확보 (JVM 시각 대신 DB 시각 사용 — 시차 오차 방지)
        LocalDateTime batchTime = jdbcTemplate.queryForObject("SELECT now()", LocalDateTime.class);

        log.debug("batchTime: {}", batchTime);

        // batchUpdate(청크 단위 분할)
        for (int from = 0; from < size; from += BATCH_SIZE) {
            int chunk = Math.min(BATCH_SIZE, size - from);
            int fromIndex = from;

            jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    BookCreateReq b = bookList.get(fromIndex + i);
                    ps.setString(1, b.getTitle());
                    ps.setString(2, b.getAuthor());
                    ps.setString(3, b.getPublisher());
                    ps.setString(4, b.getCategory());
                    ps.setString(5, b.getLevel());
                    ps.setInt(6, b.getDifficulty());
                    ps.setLong(7, b.getBookNo());
                    ps.setString(8, b.getImageUrl());
                }

                @Override
                public int getBatchSize() {
                    return chunk;
                }
            });
        }

        // 배치 후 SELECT — registered_at 기준으로 삽입 건수 산출
        Long inserted = jdbcTemplate.queryForObject(COUNT_INSERTED_SQL, Long.class, batchTime);
        if (inserted == null) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        long updated = 0;
        long skipped = size - inserted;

        log.debug("book upsert result — inserted: {}, updated: {}, skipped: {}",
                inserted, updated, skipped);

        return new UpsertResult(inserted, updated, skipped, batchTime);
    }
}
