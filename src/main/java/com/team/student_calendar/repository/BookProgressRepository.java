package com.team.student_calendar.repository;

import com.team.student_calendar.entity.BookProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookProgressRepository extends JpaRepository<BookProgressEntity, Long> {

    Optional<BookProgressEntity> findByStudentId(Long studentId);
}
