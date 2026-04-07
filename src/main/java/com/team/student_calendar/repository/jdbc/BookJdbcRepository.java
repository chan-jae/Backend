package com.team.student_calendar.repository.jdbc;


import com.team.student_calendar.dto.BookCreateReq;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface BookJdbcRepository {

    int BATCH_SIZE = 150;

    JdbcTemplate jdbcTemplate();

    String insertSql();

    default int bulkInsertBooks(List<BookCreateReq> bookList) {
        int size = bookList.size();
        if (size == 0) {
            return 0;
        }
        JdbcTemplate jt = jdbcTemplate();
        String sql = insertSql();
        for (int from = 0; from < size; from += BATCH_SIZE) {
            int chunk = Math.min(BATCH_SIZE, size - from);
            int fromIndex = from;
            jt.batchUpdate(sql, new BatchPreparedStatementSetter() {

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
        return size;
    }
}
