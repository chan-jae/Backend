package com.team.student_calendar.repository;

import com.team.student_calendar.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.team.student_calendar.dto.BookSliderRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // 문학, 안 읽은 책 중 난이도 최하 1권
    Optional<BookEntity> findTop1ByCategoryAndLevelAndIdNotInOrderByDifficultyAsc(
            String category, String level, List<Long> readBookIds);

    // 비문학
    Optional<BookEntity> findTop1ByCategoryNotAndLevelAndIdNotInOrderByDifficultyAsc(
            String category, String level, List<Long> readBookIds);
}
