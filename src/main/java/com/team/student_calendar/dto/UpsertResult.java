package com.team.student_calendar.dto;

/**
 * UPSERT 실행 결과
 *
 * @param insertedCount 신규 삽입된 행 수
 * @param updatedCount  기존 행이 실제로 갱신된 행 수 (IS DISTINCT FROM 조건에 걸린 것)
 * @param skippedCount  충돌했지만 값이 동일해 변경 없이 스킵된 행 수
 */
public record UpsertResult(long insertedCount, long updatedCount, long skippedCount) {

    public long totalAffected() {
        return insertedCount + updatedCount;
    }
}
