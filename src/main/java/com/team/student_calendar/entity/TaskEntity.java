package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "task", schema = "student_calendar")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private StudentEntity studentEntity;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "due_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime dueAt;

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private boolean isCompleted = false; // 기본값은 '미완료(false)'
    public void complete() {
        this.isCompleted = true;
    }
    // 완료 취소
    public void incomplete() {
        this.isCompleted = false; // 상태를 다시 false(미완료)로 되돌림
    }
}
