package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "student", schema = "student_calendar")

public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 20)
    private String loginId;

    @Column(name = "grade", nullable = false, length = 10)
    private String grade;

    @Column(name = "level", nullable = false, length = 10)
    private String level;
}
