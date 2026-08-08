package com.team.student_calendar.repository.impl;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.ExcelBookReq;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Profile("postgres")
@Repository
@RequiredArgsConstructor
public class BookPostgresRepository implements BookJdbcRepository {

    // registered_at 은 DB 함수 now() 로 세팅
    private static final String INSERT_SQL = """
            INSERT INTO student_calendar.book
            (title, author, publisher, category, "level", difficulty, book_no, image_url, c_level, b_hash, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            ON CONFLICT (book_no)
            DO UPDATE SET
                updated_at = now(),
                state = 0
            """;

    // 이번 배치에서 갱신되지 않은 책은 state를 1로 표시
    private static final String MARK_STALE_BOOKS_SQL = """
            UPDATE student_calendar.book
            SET state = 1
            WHERE type = 0 AND updated_at < ?
            """;

    // 액셀에 입력한 책 삽입
    private static final String INSERT_SQL_BY_EXCEL = """
            INSERT INTO student_calendar.book
            (title, author, publisher, category, "level", difficulty, type, is_active, c_level, b_hash, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UpsertResult bulkInsertBooks(List<BookCreateReq> bookList) {

        int size = bookList.size();
        if (size == 0) {
            return new UpsertResult(0, 0, 0, null);
        }

        // 배치 시작 직전 DB 서버 시각 확보 (JVM 시각 대신 DB 시각 사용 — 시차 오차 방지)
        LocalDateTime batchTime = jdbcTemplate.queryForObject("SELECT now()", LocalDateTime.class);

        long baseSize = Optional.ofNullable(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student_calendar.book", Long.class))
                        .orElse(0L);

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
                    ps.setByte(9, BookLevelMapping.customLevelOf(b.getLevel()));
                    ps.setString(10, BookHashUtil.generateBookHashKey(b.getTitle(), b.getAuthor()));
                }

                @Override
                public int getBatchSize() {
                    return chunk;
                }
            });
        }

        long afterSize = Optional.ofNullable(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student_calendar.book", Long.class))
                .orElse(0L);

        int staleCount = jdbcTemplate.update(MARK_STALE_BOOKS_SQL, batchTime);

        long inserted = afterSize - baseSize;
        long updated = staleCount;
        long skipped = baseSize;

        log.debug("book upsert result — inserted: {}, updated: {}, skipped: {}",
                inserted, updated, skipped);

        return new UpsertResult(inserted, updated, skipped, batchTime);
    }


    @Override
    public UpsertResult bulkInsertBooksByExcel(List<BookCreateReq> bookList) {

        int size = bookList.size();
        if (size == 0) {
            return new UpsertResult(0, 0, 0, null);
        }

        LocalDateTime batchTime = jdbcTemplate.queryForObject("SELECT now()", LocalDateTime.class);

        log.debug("batchTime: {}", batchTime);

        // Service 레이어에서 책권수 조절하므로 여기서는 그냥 한번에 삽입
        int[] results = jdbcTemplate.batchUpdate(INSERT_SQL_BY_EXCEL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                BookCreateReq b = bookList.get(i);
                ps.setString(1, b.getTitle());
                ps.setString(2, b.getAuthor());
                ps.setString(3, b.getPublisher());
                ps.setString(4, b.getCategory());
                ps.setString(5, b.getLevel());
                ps.setInt(6, b.getDifficulty());
                ps.setByte(7, (byte) 1);
                ps.setByte(8, b.getIsActive());
                ps.setByte(9, BookLevelMapping.customLevelOf(b.getLevel()));
                ps.setString(10, BookHashUtil.generateBookHashKey(b.getTitle(), b.getAuthor()));
            }

            @Override
            public int getBatchSize() {
                return size;
            }
        });

        return new UpsertResult(results.length, 0, 0, batchTime);
    }
}
