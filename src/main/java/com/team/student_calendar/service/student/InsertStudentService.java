package com.team.student_calendar.service.student;

import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.repository.jdbc.StudentJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class InsertStudentService {

    private final StudentJdbcRepository studentJdbcRepository;

    /**
     * List 타입 학생들을 UPSERT 저장.
     *
     * @param studentCreateReqList 저장할 학생 목록
     * @return 삽입 데이터, 수정 데이터, 영향 안받은 데이터
     */
    @Transactional
    public UpsertResult saveStudentList(List<StudentCreateReq> studentCreateReqList) {

        log.info("try to upsert {} students", studentCreateReqList.size());

        UpsertResult result = studentJdbcRepository.bulkInsertStudents(studentCreateReqList);

        log.info("upsert complete — inserted: {}, updated: {}, skipped(no change): {}",
                result.insertedCount(), result.updatedCount(), result.skippedCount());

        return result;
    }
}
