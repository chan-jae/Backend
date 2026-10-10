package com.team.student_calendar.dto;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;

/**
 * 마이북 정보 수정 요청 (요청 body는 {책 id: 이 객체} 형태)
 * @param isActive 활성화 여부 (1: 활성, 0: 비활성)
 */
public record MyBookUpdateReq(
        Integer difficulty,
        Byte isActive
) {

    /**
     * 필수값 / 길이(book 테이블 컬럼 길이) 검증
     * @param bookId 에러 메시지에 표시할 책 id
     */
    public void validate(Long bookId) {

        String error = null;
         if (difficulty == null || difficulty <= 0) {
            error = "난이도는 0보다 커야 합니다.";
        } else if (isActive == null || (isActive != 0 && isActive != 1)) {
            error = "활성화 여부는 0 또는 1이어야 합니다.";
        }

        if (error != null) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR, "책 id %d: %s".formatted(bookId, error));
        }
    }
}
