package com.team.student_calendar.security.repository;

import com.team.student_calendar.security.entity.RegisterTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RegisterTokenRepository extends JpaRepository<RegisterTokenEntity, Long> {

    Optional<RegisterTokenEntity> findByToken(String token);

    void deleteByRegisteredAtBefore(LocalDateTime cutoff);

    // @Modifying 은 1차 캐시를 거치지않고 바로 DB에 반영되기 때문에 1차 캐시에서는
    // 갱신이 안되기 때문에 다시 사용해야한다면 clearAutomatically = true 옵션을 추가해서 비워줘야함
    @Modifying
    @Query("update RegisterTokenEntity r set r.used = true where r.token = :token and r.used = false")
    int consumeToken(@Param("token") String token);
}
