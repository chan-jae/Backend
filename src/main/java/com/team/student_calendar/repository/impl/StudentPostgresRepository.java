package com.team.student_calendar.repository.impl;

import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.repository.jdbc.StudentJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("postgres")
@Repository
@RequiredArgsConstructor
public class StudentPostgresRepository implements StudentJdbcRepository {

    // // insertSql() 호환용 단일 행 템플릿 (BookJdbcRepository 등 다른 곳에서 재사용 시 사용)
    // private static final String INSERT_SQL_TEMPLATE = """
    // INSERT INTO student_calendar.student
    // (name, login_id, phone, grade, "level", account_no, state, joined_at)
    // VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    // ON CONFLICT (account_no)
    // DO UPDATE SET
    // name = EXCLUDED.name,
    // login_id = EXCLUDED.login_id,
    // phone = EXCLUDED.phone,
    // grade = EXCLUDED.grade,
    // "level" = EXCLUDED."level",
    // state = EXCLUDED.state
    // WHERE
    // (student.name, student.login_id, student.phone, student.grade,
    // student."level", student.state)
    // IS DISTINCT FROM
    // (EXCLUDED.name, EXCLUDED.login_id, EXCLUDED.phone, EXCLUDED.grade,
    // EXCLUDED."level", EXCLUDED.state)
    // """;
    private static final String INSERT_HEAD = """
            INSERT INTO student_calendar.student
                (name, login_id, phone, grade, "level", account_no, state, joined_at, updated_at)
            VALUES
            """;

    private static final String UPSERT_SUFFIX = """
            ON CONFLICT (account_no)
            DO UPDATE SET
                name       = EXCLUDED.name,
                login_id   = EXCLUDED.login_id,
                phone      = EXCLUDED.phone,
                grade      = EXCLUDED.grade,
                "level"    = EXCLUDED."level",
                state      = EXCLUDED.state,
                updated_at = now()
            RETURNING (xmax = 0) AS is_new
            """;

    private static final String DELETE_SQL = """
            DELETE FROM student_calendar.student_book
            WHERE student_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 다중 VALUES + RETURNING + xmax 방식으로 UPSERT 실행.
     * <p>
     * 충돌 시 데이터 동일 여부와 관계없이 항상 updated_at을 갱신한다.
     * </p>
     * <ul>
     * <li>xmax = 0 → 신규 삽입된 행</li>
     * <li>xmax ≠ 0 → 충돌하여 갱신된 행 (데이터 변경 + 동일 모두 포함)</li>
     * </ul>
     */
    @Transactional
    @Override
    public UpsertResult bulkInsertStudents(List<StudentCreateReq> studentList) {

        if (studentList.isEmpty()) {
            return new UpsertResult(0, 0, 0, null);
        }

        LocalDateTime updateBaseTime = jdbcTemplate.queryForObject("SELECT now()", LocalDateTime.class);

        // VALUES 절 동적 생성
        String valuesClause = studentList.stream()
                .map(b -> "(?, ?, ?, ?, ?, ?, ?, ?, now())")
                .collect(Collectors.joining(",\n"));

        String sql = INSERT_HEAD + valuesClause + "\n" + UPSERT_SUFFIX;

        // 파라미터 평탄화 (행 순서 × 컬럼 8개)
        List<Object> args = new ArrayList<>(studentList.size() * 8);
        for (StudentCreateReq b : studentList) {
            args.add(b.getName());
            args.add(b.getLoginId());
            args.add(b.getPhone());
            args.add(b.getGrade());
            args.add(b.getLevel());
            args.add(b.getAccountNo());
            args.add(b.getStateStr());
            args.add(b.getJoinedAt());
        }

        // RETURNING 결과를 is_new(boolean) 목록으로 수신
        List<Boolean> returning = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getBoolean("is_new"),
                args.toArray());

        // true인 것만 실제로 삽입된 것
        long inserted = returning.stream().filter(v -> v).count();
        // false인 것은 수정된 것
        long updated = returning.size() - inserted;

        return new UpsertResult(inserted, updated, 0, updateBaseTime);
    }

    @Override
    public void bulkDeleteStudent(Long id) {

    }
}
