package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.entity.BookEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomBookCreateReq {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotNull(message = "난이도는 필수 항목입니다.")
    @Positive(message = "난이도는 0 이상의 정수여야 합니다.")
    private Integer difficulty;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    /**
     * 직접 등록 책을 기존 book 테이블에 저장하기 위해 BookEntity 로 변환한다.
     * <p>
     * book 테이블의 NOT NULL 컬럼(author/publisher/book_no/image_url/level/c_level/state/updated_at)을
     * DB 스키마 수정 없이 통과시키기 위해 더미 값을 강제 매핑한다. {@code type=CUSTOM(1)} 으로 구분한다.
     * </p>
     */
    public BookEntity toEntity() {
        return BookEntity.builder()
                .title(this.title)
                .category(this.category)
                .difficulty(this.difficulty)
                .type((byte) BookType.CUSTOM.getType())   // 1 = 직접 등록
                .author("자체 등록")
                .publisher("용천점")
                .level("CUSTOM_" + this.difficulty)
                .cLevel((byte) 99)
                .bookNo(System.currentTimeMillis())        // unique 제약 회피용
                .imageUrl("")
                .state((byte) 0)                           // NOT NULL — 기본 상태
                .updatedAt(LocalDateTime.now())            // NOT NULL — 자동 타임스탬프 없음
                .build();
    }
}
