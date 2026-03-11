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

    // 쿼리 = 전체 책 + 읽음 상태를 번호순으로 가져옴
    @Query("SELECT new com.team.student_calendar.dto.BookSliderRes(" +
            "b.bookNo, b.title, b.author, b.category, b.imageUrl, " +
            "(CASE WHEN bp.id IS NOT NULL THEN true ELSE false END)) " +
            "FROM BookEntity b " +
            "LEFT JOIN BookProgressEntity bp ON bp.bookEntity.id = b.id AND bp.studentEntity.id = :studentId " +
            "ORDER BY b.bookNo ASC")
    List<BookSliderRes> findAllBooksWithReadStatus(@Param("studentId") Long studentId);
}
