package com.team.student_calendar.repository;

import com.team.student_calendar.common.enums.ApiUsageType;
import com.team.student_calendar.entity.ApiUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface ApiUsageRepository extends JpaRepository<ApiUsageEntity, ApiUsageType> {

    // 한도 안이면 +1 (마지막 호출이 오늘 이전이면 1로 초기화), 한도 초과면 0 반환
    // 확인과 증가를 UPDATE 한 번으로 해서 동시 요청이 같이 통과하지 않게
    @Modifying
    @Query("""
            UPDATE ApiUsageEntity u
            SET u.callCount = CASE WHEN u.updatedAt >= :todayStart THEN u.callCount + 1 ELSE 1 END,
                u.updatedAt = :now
            WHERE u.api = :api AND (u.updatedAt < :todayStart OR u.callCount < :limit)
            """)
    int increaseCallCount(@Param("api") ApiUsageType api, @Param("limit") int limit,
                          @Param("todayStart") LocalDateTime todayStart, @Param("now") LocalDateTime now);
}
