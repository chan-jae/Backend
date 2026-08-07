package com.team.student_calendar.common.util;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DtoValidator {

    private final Validator validator;

    /**
     * Bean Validation 메서드 검증 (@Valid 없이)
     * @param dto
     * @param dto 타입
     */
    public <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR, message);
        }
    }

}
