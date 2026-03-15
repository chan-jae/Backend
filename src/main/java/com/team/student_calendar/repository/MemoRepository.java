package com.team.student_calendar.repository;

import com.team.student_calendar.entity.MemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoRepository extends JpaRepository<MemoEntity, Long> {

    // studentId가 작성한 모든 메모를 가져오게 함
    List<MemoEntity> findByStudentEntity_Id(Long studentId);
}