package com.team.student_calendar.repository;

import com.team.student_calendar.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByIdAndBook_Id(Long id, Long bookId);

    boolean existsAllByBook_Id(Long id);

    Optional<FileEntity> findFirstByBook_Id(Long bookId);
}
