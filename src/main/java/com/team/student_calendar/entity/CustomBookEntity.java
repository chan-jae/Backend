package com.team.student_calendar.entity;

import com.team.student_calendar.dto.RecBookRes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 티칭오션에 없는 책을 교사가 직접 등록하는 도서.
 * <p>
 * 기존 {@link BookEntity}(외부 동기화 데이터)와 완전히 분리된 테이블이며,
 * 제목/난이도/카테고리만 가진다. 난이도는 기존 A_0/cLevel 체계가 아닌 단순 정수(1,2,3,...)다.
 * 시험문제는 추후 별도 테이블로 1:N 확장 예정.
 * </p>
 */
@DynamicInsert
@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "custom_book",
        schema = "student_calendar",
        indexes = {
                @Index(
                        name = "idx_custom_book_category_difficulty",
                        columnList = "category ASC, difficulty ASC"
                )
        }
)
public class CustomBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    /**
     * 추천 응답 DTO로 변환. custom=true 로 표시해 프론트가 상세 조회 시
     * /api/custom-books/{id} 로 라우팅할 수 있게 한다.
     */
    public RecBookRes toRecBook() {
        RecBookRes r = new RecBookRes();
        r.setId(this.id);
        r.setTitle(this.title);
        r.setCategory(this.category);
        r.setDifficulty(this.difficulty);
        r.setUpdatedAt(this.updatedAt);
        r.setCustom(true);
        return r;
    }
}
