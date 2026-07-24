package com.team.student_calendar.repository;

import com.team.student_calendar.dto.RecBookMapping;
import com.team.student_calendar.entity.BookEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

        @Override
        @EntityGraph(attributePaths = { "file" })
        @NullMarked
        @Cacheable(cacheNames = "books", key = "'all'")
        List<BookEntity> findAll(Sort sort);


        // ===== 직접 등록(custom) 책 CRUD 용 (type = CUSTOM(1)) =====

        // custom 책 전체 (목록 조회)
        List<BookEntity> findAllByType(Byte type, Sort sort);

        // custom 책 단건 (id 로 조회하되 custom 인지 함께 검증)
        Optional<BookEntity> findByIdAndType(Long id, Byte type);

//        List<BookEntity> findAllByBookNoIn(List<Long> bookNoList);

//        List<BookEntity> findAllByIdIn(List<Long> idList);

//        long countAllByIdIn(List<Long> idList);

//        // 검색
//        @Query("SELECT b FROM BookEntity b WHERE REPLACE(b.title, ' ', '') LIKE %:keyword% " +
//                        "OR REPLACE(b.author, ' ', '') LIKE %:keyword% " +
//                        "OR REPLACE(b.publisher, ' ', '') LIKE %:keyword%")
//        List<BookEntity> searchBooksByKeyword(@Param("keyword") String keyword);

//        // 문학, 안 읽은 책 중 난이도 최하 1권
//        Optional<BookEntity> findTop1ByCategoryAndLevelAndIdNotInOrderByDifficultyAsc(
//                        String category, String level, List<Long> readBookIds);
//
//        // 비문학
//        Optional<BookEntity> findTop1ByCategoryNotAndLevelAndIdNotInOrderByDifficultyAsc(
//                        String category, String level, List<Long> readBookIds);


        // 읽어야 하는 문학 옵션에 따라 가져오기
        @Query("""
                        SELECT
                        b as book, sb.state as state, sb.readAt as readAt FROM BookEntity b
                        LEFT JOIN StudentBookEntity sb ON b.id = sb.book.id
                        AND sb.student.id = :studentId
                        WHERE b.category = :#{T(com.team.student_calendar.common.enums.BookCategory).LITERATURE.name()}
                        AND b.state = 0
                        AND (
                            (sb.state IS NULL AND b.cLevel >= :baseLevel)
                            OR
                            (sb.state != 2)
                        )
        """)
        List<RecBookMapping> findFirstUnreadBookAboveCLevelForLiterature(
                        @Param("studentId") Long studentId,
                        @Param("baseLevel") Byte baseLevel,
                        Pageable pageable
        );

        // 읽어야 하는 비문학 옵션에 따라 가져오기
        @Query("""
                        SELECT
                        b as book, sb.state as state, sb.readAt as readAt FROM BookEntity b
                        LEFT JOIN StudentBookEntity sb ON b.id = sb.book.id
                        AND sb.student.id = :studentId
                        WHERE b.category != :#{T(com.team.student_calendar.common.enums.BookCategory).LITERATURE.name()}
                        AND b.state = 0
                        AND (
                            (sb.state IS NULL AND b.cLevel >= :baseLevel)
                            OR
                            (sb.state != 2)
                        )
        """)
        List<RecBookMapping> findFirstUnreadBookAboveCLevelForNonLiterature(
                        @Param("studentId") Long studentId,
                        @Param("baseLevel") Byte baseLevel,
                        Pageable pageable
        );
}
