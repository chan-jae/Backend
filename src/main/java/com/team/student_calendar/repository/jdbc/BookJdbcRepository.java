package com.team.student_calendar.repository.jdbc;

import com.team.student_calendar.dto.BookCreateReq;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookJdbcRepository {

    private static final int BATCH_SIZE = 500;

    private static final String INSERT_SQL = """
            INSERT IGNORE INTO student_calendar.book
            (title, author, publisher, category, `level`, difficulty, book_no, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public int bulkInsertBooks(List<BookCreateReq> bookList) {

        int size = bookList.size();
        if (size == 0) {
            return 0;
        }

        int totalInserted = 0;
        for (int from = 0; from < size; from += BATCH_SIZE) {
            int chunk = Math.min(BATCH_SIZE, size - from);
            int fromIndex = from;
            int[] results = jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {

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
            totalInserted += Arrays.stream(results).sum();
        }

        return totalInserted;
    }
}
