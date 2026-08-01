package com.team.student_calendar.dto;

import com.team.student_calendar.common.constant.LevelRegexPattern;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ManualBookDto {

    @NotBlank(message = "제목은 필수 항목입니다.")
    @Size(max = 50, message = "제목은 50자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "저자는 필수 항목입니다.")
    @Size(max = 20, message = "저자는 50자 이하로 입력해주세요.")
    private String author;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    @Size(max = 20, message = "카테고리는 20자 이하로 입력해주세요.")
    private String category;

    @NotBlank(message = "출판사는 필수 항목입니다.")
    @Size(max = 20, message = "출판사는 50자 이하로 입력해주세요.")
    private String publisher;

    @NotBlank(message = "레벨은 필수 항목입니다.")
    @Size(max = 4, message = "올바른 레벨값인지 확인해주세요.")
    private String level;

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
        // 레벨 검증
        LevelRegexPattern.validate(this.level);
        // 난이도 검증(해당 레벨 범위 맞는지)
        LevelDifficultyRange.validate(this.level, this.difficulty);
        // 책 타입 검증
        BookType.validate(this.type);
    }
}
