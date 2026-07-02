package com.team.student_calendar.service.book;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.CustomBookCreateReq;
import com.team.student_calendar.entity.CustomBookEntity;
import com.team.student_calendar.repository.CustomBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomBookService {

    private final CustomBookRepository customBookRepository;

    /** 카테고리 → 난이도 → 제목 순 정렬 */
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("category"),
            Sort.Order.asc("difficulty"),
            Sort.Order.asc("title"));

    /**
     * 직접 등록 책 저장
     * @param req 제목, 난이도, 카테고리
     * @return 저장된 엔티티
     */
    @Transactional
    public CustomBookEntity create(CustomBookCreateReq req) {

        validateCategory(req.getCategory());

        CustomBookEntity saved = customBookRepository.save(req.toEntity());
        log.info("custom book created — id: {}, title: {}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 직접 등록 책 전체 조회 (카테고리·난이도순)
     */
    @Transactional(readOnly = true)
    public List<CustomBookEntity> findAll() {
        return customBookRepository.findAll(DEFAULT_SORT);
    }

    /**
     * 직접 등록 책 단건 조회
     */
    @Transactional(readOnly = true)
    public CustomBookEntity findById(Long id) {
        return customBookRepository.findById(id)
                .orElseThrow(() -> new BaseException(BookErrorCode.CUSTOM_BOOK_NOT_FOUND));
    }

    /**
     * 직접 등록 책 수정
     */
    @Transactional
    public CustomBookEntity update(Long id, CustomBookCreateReq req) {

        validateCategory(req.getCategory());

        CustomBookEntity entity = findById(id);
        entity.setTitle(req.getTitle());
        entity.setDifficulty(req.getDifficulty());
        entity.setCategory(req.getCategory());
        return entity;
    }

    /**
     * 추천 fallback: 특정 카테고리의 custom 책을 난이도·제목 오름차순으로 최대 {@code limit}권.
     * <p>커리큘럼 책이 부족한 슬롯을 채우기 위해 {@code RecommendBookService}에서 호출된다.</p>
     *
     * @param category LITERATURE / NON_LITERATURE
     * @param limit    필요한 권수 (0 이하이면 빈 리스트)
     */
    @Transactional(readOnly = true)
    public List<CustomBookEntity> findForRecommend(String category, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        Sort sort = Sort.by(
                Sort.Order.asc("difficulty"),
                Sort.Order.asc("title"));

        return customBookRepository.findByCategory(category, PageRequest.of(0, limit, sort));
    }

    /**
     * 직접 등록 책 삭제
     */
    @Transactional
    public void delete(Long id) {
        CustomBookEntity entity = findById(id);
        customBookRepository.delete(entity);
        log.info("custom book deleted — id: {}", id);
    }

    /** 카테고리는 LITERATURE / NON_LITERATURE 만 허용 (ALL 불가) */
    private void validateCategory(String category) {
        try {
            BookCategory parsed = BookCategory.valueOf(category);
            if (parsed == BookCategory.ALL) {
                throw new BaseException(BookErrorCode.INVALID_CATEGORY);
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }
}
