package com.team.student_calendar.repository;

import com.team.student_calendar.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.team.student_calendar.dto.BookSliderRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    List<BookEntity> findAllByBookNoIn(List<Long> bookNoList);

    List<BookEntity> findAllByIdIn(List<Long> idList);

    long countAllByIdIn(List<Long> idList);

    // 검색
    @Query("SELECT b FROM BookEntity b WHERE REPLACE(b.title, ' ', '') LIKE %:keyword% " +
            "OR REPLACE(b.author, ' ', '') LIKE %:keyword% " +
            "OR REPLACE(b.publisher, ' ', '') LIKE %:keyword%")
    List<BookEntity> searchBooksByKeyword(@Param("keyword") String keyword);
}
