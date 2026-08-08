package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExcelBookReq {

    @NotBlank(message = "제목은 필수 항목입니다.")
    @Size(max = 50, message = "제목은 50자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "저자는 필수 항목입니다.")
    @Size(max = 20, message = "저자는 20자 이하로 입력해주세요.")
    private String author;

    @NotBlank(message = "출판사는 필수 항목입니다.")
    @Size(max = 20, message = "출판사는 20자 이하로 입력해주세요.")
    private String publisher;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    @Pattern(regexp = "L|NL", message = "카테고리는 L, NL 중에서만 입력해주세요.")
    private String category;

    @NotBlank(message = "레벨은 필수 항목입니다.")
    @Pattern(regexp = "\\d{1,2}[AB]", message = "올바른 레벨 형식이 아닙니다. (ex: 2A, 10B)")
    private String level;

    @NotBlank(message = "활성화 여부는 필수 항목입니다.")
    @Pattern(regexp = "[10]", message = "활성화 여부는 1, 0 중에서만 입력해주세요.")
    private String isActive;



    public BookCreateReq toBookCreateReq() {

        BookCategory bookCategory;
        if ("L".equals(this.category)) {
            bookCategory = BookCategory.LITERATURE;
        } else if ("NL".equals(this.category)) {
            bookCategory = BookCategory.NON_LITERATURE;
        } else {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY, "카테고리는 L, NL 중에서만 입력해주세요.");
        }

        // ex) "5A" -> "A_5"
        String letter = this.level.substring(this.level.length() - 1);
        String digits = this.level.substring(0, this.level.length() - 1);
        String convertedLevel = letter + "_" + digits;

        // low, high 평균값(소수점 버림)
        LevelDifficultyRange range = LevelDifficultyRange.of(convertedLevel);
        int difficulty = (range.getLow() + range.getHigh()) / 2;

        return BookCreateReq.builder()
                .title(this.title)
                .author(this.author)
                .publisher(this.publisher)
                .category(bookCategory.name())
                .level(convertedLevel)
                .difficulty(difficulty)
                .isActive(Byte.valueOf(this.isActive))
                .type(BookType.CUSTOM.getType())
                .build();
    }
}
