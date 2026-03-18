package com.team.student_calendar.repository;

import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentBookRepository extends JpaRepository<StudentBookEntity, Long> {

    Optional<StudentBookEntity> findByStudentIdAndBookId(Long studentId, Long bookId);

    List<StudentBookEntity> findByStudentId(Long studentId);

    List<StudentBookEntity> findByStudentIdAndBook_Category(Long studentId, String category);

    List<StudentBookEntity> findByStudentIdAndBook_CategoryNot(Long studentId, String category);

    long deleteAllByStudent(StudentEntity student);

    long deleteByStudentAndBook(StudentEntity student, BookEntity book);
}
