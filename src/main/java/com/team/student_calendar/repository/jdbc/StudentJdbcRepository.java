package com.team.student_calendar.repository.jdbc;

import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.dto.UpsertResult;

import java.util.List;

public interface StudentJdbcRepository {

    /**
     * UPSERT 후 RETURNING + xmax 로 삽입/갱신/스킵 건수를 정확히 반환.
     */
    UpsertResult bulkInsertStudents(List<StudentCreateReq> studentList);
}
