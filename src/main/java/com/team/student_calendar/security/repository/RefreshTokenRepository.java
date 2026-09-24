package com.team.student_calendar.security.repository;

import com.team.student_calendar.security.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    boolean existsByRefresh(String refreshToken);

    void deleteByRefresh(String refreshToken);

    Optional<RefreshTokenEntity> findByRefresh(String refreshToken);

    @Query("select distinct r.username from RefreshTokenEntity r")
    List<String> findDistinctUsernames();

    void deleteByUsernameAndRegisteredAtBefore(String username, LocalDateTime cutoff);

    List<RefreshTokenEntity> findByUsernameOrderByRegisteredAtDesc(String username);
}
