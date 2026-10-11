package com.team.student_calendar.service.isbnlookup;

import com.team.student_calendar.dto.IsbnBookRes;
import com.team.student_calendar.repository.IsbnLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectIsbnLookupService {

    private final IsbnLookupRepository isbnLookupRepository;


    /**
     * ISBN 조회 기록 전체를 최신순으로 가져오기
     * @return 조회 기록 list
     */
    @Transactional(readOnly = true)
    public List<IsbnBookRes> findAll() {
        return isbnLookupRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(IsbnBookRes::from)
                .toList();
    }
}
