package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

@Data
public class ManualUpdateBookDto {

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    @Size(max = 20, message = "카테고리는 20자 이하로 입력해주세요.")
    private String category;

    @NotNull(message = "난이도는 필수 항목입니다.")
    @Positive(message = "난이도는 0보다 커야 합니다.")
    private Integer difficulty;

    @NotBlank(message = "타입은 필수 항목입니다.")
    @Size(max = 20, message = "타입은 20자 이하로 입력해주세요.")
    private String type;

    @NotBlank(message = "활성화 여부는 필수 항목입니다.")
    @Pattern(regexp = "true|false", message = "올바른 활성화 여부가 아닙니다.")
    private String isActive;



    public void validate() {

        // 카테고리 검증
        BookCategory.validateForCreate(this.category);
        // 커스텀 타입만 가능
        BookType.validate(this.type);
        if (!BookType.CUSTOM.name().equals(type)) {
            throw new BaseException(BookErrorCode.INVALID_TYPE);
        }
    }


    /**
     * 기존 @Valid로 체크하던 것 + validate() 모두 검증
     * @return 에러 메시지 반환 (문제 없으면 null)
     */
    public String validateAll() {

        if (category == null || category.isBlank()) {
            return "카테고리는 필수 항목입니다.";
        }
        if (category.length() > 20) {
            return "카테고리는 20자 이하로 입력해주세요.";
        }



        if (difficulty == null) {
            return "난이도는 필수 항목입니다.";
        }
        if (difficulty <= 0) {
            return "난이도는 0보다 커야 합니다.";
        }

        if (type == null || type.isBlank()) {
            return "타입은 필수 항목입니다.";
        }
        if (type.length() > 20) {
            return "타입은 20자 이하로 입력해주세요.";
        }

        if (isActive == null || isActive.isBlank()) {
            return "활성화 여부는 필수 항목입니다.";
        }
        if (!isActive.equals("true") && !isActive.equals("false")) {
            return "올바른 활성화 여부가 아닙니다.";
        }

        try {
            validate();
        } catch (BaseException e) {
            return e.getMessage();
        }

        return null;
    }
}
