package com.team.student_calendar.repository;

import com.team.student_calendar.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    List<BookEntity> findAllByBookNoIn(List<Integer> bookNoList);
}
