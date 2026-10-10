package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.dto.ManualUpdateBookDto;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.IsbnLookupRepository;
import com.team.student_calendar.service.file.ReUploadFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateBookService {

    private final SelectBookService selectBookService;
    private final ValidateBookDupService validateBookDupService;
    private final IsbnLookupRepository isbnLookupRepository;
    private final ReUploadFileService reUploadFileService;

    /** 마이북 일괄 수정 구분: 레벨(난이도) 변경 */
    public static final String LEVEL = "LEVEL";
    /** 마이북 일괄 수정 구분: 활성화 (isActive = 1) */
    public static final String ACTIVE = "ACTIVE";
    /** 마이북 일괄 수정 구분: 비활성화 (isActive = 0) */
    public static final String INACTIVE = "INACTIVE";




    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public BookEntity updateBook(Long id, ManualUpdateBookDto req) {

        log.info("try to update book: {}", id);

        // 필드 검증
        req.validate();

        BookEntity entity = selectBookService.findById(id);

        // 커스텀 책만 수정가능
        if (BookType.CUSTOM.getType() != entity.getType()) {
            throw new BaseException(BookErrorCode.INVALID_TYPE, "수정 가능한 책 타입이 아닙니다.");
        }

//        // 제목/저자가 바뀐 경우에만 등록된 책 있는지 체크
//        String newHash = BookHashUtil.generateBookHashKey(req.getTitle(), req.getAuthor());
//        if (!newHash.equals(entity.getBHash())) {
//            validateBookDupService.checkBookDuplication(req.getTitle(), req.getAuthor());
//        }

        byte isActive = (byte) (Boolean.parseBoolean(req.getIsActive()) ? 1 : 0);

//        entity.setTitle(req.getTitle());
//        entity.setAuthor(req.getAuthor());
//        entity.setPublisher(req.getPublisher());
        entity.setDifficulty(req.getDifficulty());
        entity.setCategory(req.getCategory());
//        entity.setLevel(req.getLevel());
//        entity.setCLevel(BookLevelMapping.customLevelOf(req.getLevel()));
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setIsActive(isActive);
//        entity.setBHash(newHash);
        entity.setUpdatedAt(LocalDateTime.now());

        // 활동지가 있는 마이북이면 JSON 책 정보 갱신 후 PDF 다시 업로드 (DB 반영 먼저, S3는 마지막)
        refreshQuestionSheet(entity).ifPresent(sheet -> {
            isbnLookupRepository.flush();
            reUploadFileService.reuploadMyBookPdf(entity, sheet);
        });

        log.info("book update complete - book: {}", id);

        return entity;
    }


    /**
     * 마이북 여러 권 일괄 수정
     * LEVEL: 난이도 수정 + 활동지 JSON 안의 책 정보 갱신 + 활동지 PDF 다시 업로드 (PDF에 난이도가 들어감)
     * ACTIVE / INACTIVE: isActive를 1 / 0으로 수정 (PDF와 무관)
     * 한 트랜잭션에서 검증/DB 수정을 전부 먼저 하고 S3는 마지막 -> 한 권이라도 검증에 실패하면 아무것도 안 바뀜
     * (S3 덮어쓰기는 롤백이 안 돼서, 업로드 도중 실패하면 그 전에 덮어쓴 PDF는 새 내용으로 남음)
     * @param type 수정 구분 (LEVEL / ACTIVE / INACTIVE)
     * @param ids 수정할 책 id 목록
     * @param difficulty 바꿀 난이도 (type=LEVEL일 때만 사용)
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void updateMyBooks(String type, List<Long> ids, Integer difficulty) {

        // DB 조회 전에 요청값 검증
        switch (type) {
            case LEVEL -> {
                if (difficulty == null || difficulty <= 0 || difficulty >= 10000) {
                    throw new BaseException(CommonErrorCode.PARAMETER_ERROR, "난이도는 0보다 크고 10000보다 작아야 합니다.");
                }
            }
            case ACTIVE, INACTIVE -> { }
            default -> throw new BaseException(CommonErrorCode.PARAMETER_ERROR, "수정 구분은 LEVEL, ACTIVE, INACTIVE 중 하나여야 합니다.");
        }

        if (ids == null || ids.isEmpty()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR, "수정할 책이 없습니다.");
        }

        log.info("try to update mybooks: type={}, books={}, difficulty={}", type, ids, difficulty);

        // 활동지가 있는 책만 PDF 다시 업로드
        Map<BookEntity, QuestionSheet> sheets = new LinkedHashMap<>();

        for (Long id : ids) {
            BookEntity entity = selectBookService.findById(id);

            // 커스텀 책만 수정가능
            if (BookType.CUSTOM.getType() != entity.getType()) {
                throw new BaseException(BookErrorCode.INVALID_TYPE, "수정 가능한 책 타입이 아닙니다.");
            }

            switch (type) {
                case LEVEL -> {
                    entity.setDifficulty(difficulty);
                    refreshQuestionSheet(entity).ifPresent(sheet -> sheets.put(entity, sheet));
                }
                case ACTIVE -> entity.setIsActive((byte) 1);
                case INACTIVE -> entity.setIsActive((byte) 0);
            }
            entity.setUpdatedAt(LocalDateTime.now());
        }

        // 커밋 때가 아니라 지금 UPDATE를 날려서, DB 에러가 S3 업로드 전에 나도록
        isbnLookupRepository.flush();

        sheets.forEach(reUploadFileService::reuploadMyBookPdf);

        log.info("mybooks update complete - type: {}, books: {}, pdf re upload: {}", type, ids, sheets.size());
    }


    /**
     * 활동지 JSON 안의 책 정보를 수정된 책 정보로 갱신 (활동지 생성 전이거나 마이북이 아니면 empty)
     * @param entity 수정된 책
     * @return 갱신된 활동지
     */
    private Optional<QuestionSheet> refreshQuestionSheet(BookEntity entity) {

        return isbnLookupRepository.findFirstByBook_IdAndStatus(entity.getId(), IsbnLookupStatus.SUCCESS).map(lookup -> {
            QuestionSheet sheet = lookup.getQuestionSheet().withBook(entity);
            lookup.setQuestionSheet(sheet);
            return sheet;
        });
    }
}
