package com.team.student_calendar.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "register_token", schema = "student_calendar")
public class RegisterTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
