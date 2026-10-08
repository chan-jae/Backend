package com.team.student_calendar.repository;

import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.entity.IsbnLookupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IsbnLookupRepository extends JpaRepository<IsbnLookupEntity, Long> {

    boolean existsByIsbnAndStatusNot(String isbn, IsbnLookupStatus status);
}
