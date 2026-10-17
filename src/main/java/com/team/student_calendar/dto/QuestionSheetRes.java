package com.team.student_calendar.dto;

import java.time.Instant;

/**
 * 활동지 문제 비동기 생성 상태 (프론트 폴링용, 생성된 문제는 isbn_lookup.question_sheet에 저장)
 * @param lookupId 문제가 저장되는 ISBN 조회 기록 id
 * @param type 작업 종류 (최초 생성 / 일부 재생성, 둘 다 같은 lookupId를 쓰므로 이걸로 구분)
 * @param status 생성 상태
 * @param servedAt 완료 결과를 처음 응답한 시간 (응답 전이면 null)
 */
public record QuestionSheetRes(
        Long lookupId,
        Type type,
        Status status,
        Instant servedAt
) {

    public enum Type {
        CREATE, REGENERATE
    }

    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    public static QuestionSheetRes pending(Long lookupId, Type type) {
        return new QuestionSheetRes(lookupId, type, Status.PENDING, null);
    }

    public static QuestionSheetRes success(Long lookupId, Type type) {
        return new QuestionSheetRes(lookupId, type, Status.SUCCESS, null);
    }

    public static QuestionSheetRes failed(Long lookupId, Type type) {
        return new QuestionSheetRes(lookupId, type, Status.FAILED, null);
    }

    public QuestionSheetRes served(Instant at) {
        return new QuestionSheetRes(lookupId, type, status, at);
    }
}
