package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
