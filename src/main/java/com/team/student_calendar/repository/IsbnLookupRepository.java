package com.team.student_calendar.repository;

import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.entity.IsbnLookupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface IsbnLookupRepository extends JpaRepository<IsbnLookupEntity, Long> {

    boolean existsByIsbnAndStatusNot(String isbn, IsbnLookupStatus status);

    // 같은 ISBN으로 실패 후 재시도한 기록이 여러 개일 수 있어 가장 최근 것
    Optional<IsbnLookupEntity> findFirstByIsbnOrderByIdDesc(String isbn);

    // 책의 활동지 생성까지 성공한 조회 기록 (SUCCESS는 책당 1개)
    Optional<IsbnLookupEntity> findFirstByBook_IdAndStatus(Long bookId, IsbnLookupStatus status);

    // 지정한 상태들을 FAILED로 일괄 변경 (활동지 문제가 저장된 기록은 PDF 재업로드 대상이라 제외)
    @Modifying
    @Transactional
    @Query("""
            UPDATE IsbnLookupEntity l SET l.status = :failed, l.updatedAt = LOCAL DATETIME
            WHERE l.status IN :statuses AND l.questionSheet IS NULL AND l.book IS NULL
            """)
    int updateStatusToFailed(@Param("statuses") List<IsbnLookupStatus> statuses, @Param("failed") IsbnLookupStatus failed);

    // 활동지 문제는 있는데 PDF(file)가 없는 조회 기록 (최근 것부터)
    @Query("""
            SELECT l FROM IsbnLookupEntity l
            WHERE l.book IS NOT NULL AND l.questionSheet IS NOT NULL
              AND NOT EXISTS (SELECT f FROM FileEntity f WHERE f.book = l.book)
            ORDER BY l.id DESC
            """)
    List<IsbnLookupEntity> findQuestionSheetsWithoutPdf();
}
