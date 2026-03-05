package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

@DynamicInsert
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user", schema = "student_calendar")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "phone", nullable = false, length = 45)
    private String phone;

    @ColumnDefault("'TEACHER'")
    @Column(name = "role", nullable = false, length = 45)
    private String role;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

}