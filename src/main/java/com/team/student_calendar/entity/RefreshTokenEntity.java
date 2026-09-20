package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_token", schema = "student_calendar")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "refresh", nullable = false, length = 512)
    private String refresh;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
