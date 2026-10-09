package com.team.student_calendar.dto;

import java.time.Instant;

/**
 * 활동지 문제 비동기 생성 상태 (프론트 폴링용, 생성된 문제는 isbn_lookup.question_sheet에 저장)
 * @param lookupId 문제가 저장되는 ISBN 조회 기록 id
 * @param status 생성 상태
 * @param servedAt 완료 결과를 처음 응답한 시간 (응답 전이면 null)
 */
public record QuestionSheetRes(
        Long lookupId,
        Status status,
        Instant servedAt
) {

    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    public static QuestionSheetRes pending(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.PENDING, null);
    }

    public static QuestionSheetRes success(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.SUCCESS, null);
    }

    public static QuestionSheetRes failed(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.FAILED, null);
    }

    public QuestionSheetRes served(Instant at) {
        return new QuestionSheetRes(lookupId, status, at);
    }
}
