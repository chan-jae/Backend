package com.team.student_calendar.service.isbnlookup;

import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.IsbnBookRes;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.entity.IsbnLookupEntity;
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


    /**
     * 책의 활동지 조회 (활동지 생성까지 성공한 조회 기록의 questionSheet)
     * @param bookId 책 id
     * @return 활동지 (정답 포함)
     */
    @Transactional(readOnly = true)
    public QuestionSheet findQuestionSheetByBookId(Long bookId) {
        return isbnLookupRepository.findFirstByBook_IdAndStatus(bookId, IsbnLookupStatus.SUCCESS)
                .map(IsbnLookupEntity::getQuestionSheet)
                .orElseThrow(() -> new BaseException(BookErrorCode.QUESTION_SHEET_NOT_FOUND, "활동지가 없는 책입니다."));
    }
}
