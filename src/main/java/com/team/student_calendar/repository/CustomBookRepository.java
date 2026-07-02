package com.team.student_calendar.repository;

import com.team.student_calendar.entity.CustomBookEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomBookRepository extends JpaRepository<CustomBookEntity, Long> {

    List<CustomBookEntity> findAllByCategory(String category, Sort sort);

    /** 추천 fallback 용 — 카테고리 필터 + 페이징으로 상위 N권만 */
    List<CustomBookEntity> findByCategory(String category, Pageable pageable);
}
