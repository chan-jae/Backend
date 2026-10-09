package com.team.student_calendar.dto;

/**
 * 활동지 문제 비동기 생성 상태 (프론트 폴링용, 생성된 문제는 isbn_lookup.question_sheet에 저장)
 * @param lookupId 문제가 저장되는 ISBN 조회 기록 id
 * @param status 생성 상태
 */
public record QuestionSheetRes(
        Long lookupId,
        Status status
) {

    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    public static QuestionSheetRes pending(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.PENDING);
    }

    public static QuestionSheetRes success(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.SUCCESS);
    }

    public static QuestionSheetRes failed(Long lookupId) {
        return new QuestionSheetRes(lookupId, Status.FAILED);
    }
}
