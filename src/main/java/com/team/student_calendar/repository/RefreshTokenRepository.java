package com.team.student_calendar.repository;

import com.team.student_calendar.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    boolean existsByRefresh(String refreshToken);
}
