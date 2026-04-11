package com.team.student_calendar.repository;

import com.team.student_calendar.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    List<StudentEntity> findAllByAccountNoIn(List<Long> accountNoList);

    List<StudentEntity> findAllByOrderByJoinedAtDesc();
}
