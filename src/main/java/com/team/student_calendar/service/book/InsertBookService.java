package com.team.student_calendar.service.book;

import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.util.DtoValidator;
import com.team.student_calendar.common.util.ExcelUtil;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.ExcelBookDto;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookRepository bookRepository;
    private final BookJdbcRepository bookJdbcRepository;
    private final DtoValidator dtoValidator;


    /**
     * 책 list 저장
     * @param bookList 책 list
     * @return 삽입,갱신,스킵 건수
     */
    @Transactional
    public UpsertResult saveBookList(List<BookCreateReq> bookList) {

        log.info("try to save {} books", bookList.size());

        UpsertResult result = bookJdbcRepository.bulkInsertBooks(bookList);

        log.info("book save complete — inserted: {}, updated: {}, skipped(no change): {}",
                result.insertedCount(), result.updatedCount(), result.skippedCount());

        return result;
    }


    /**
     * 책 1권 수동 등록
     * @param req 수동 책 등록 필수 필드
     * @return BookEntity
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public BookEntity saveBook(ManualBookDto req) {

        log.info("try to save [{}] book", req.getTitle());

        // 카테고리, 레벨, 난이도, 타입 검증
        req.validate();

        byte isActive = (byte) (Boolean.parseBoolean(req.getIsActive()) ? 1 : 0);

        BookEntity bookEntity = BookEntity.builder()
                .title(req.getTitle())
                .author(req.getAuthor())
                .publisher(req.getPublisher())
                .category(req.getCategory())
                .level(req.getLevel())
                .difficulty(req.getDifficulty())
                .cLevel(BookLevelMapping.customLevelOf(req.getLevel()))
                .type(BookType.of(req.getType()).getType())
                .isActive(isActive)
                .updatedAt(LocalDateTime.now())
                .build();

        bookRepository.save(bookEntity);

        log.info("book save complete - [{}]", req.getTitle());

        return bookEntity;
    }


    /**
     * 엑셀 파일 1개를 받아 행 단위 데이터를 추출
     * @param file 엑셀 파일
     */
    public void saveBookByExcel(MultipartFile file) {

        log.info("file name: {}", file.getOriginalFilename());

        Workbook workbook = ExcelUtil.convertToWorkbook(file);

        // 실제 작성된 행이 200건 이하만 통과
        if (workbook.getSheetAt(0).getPhysicalNumberOfRows() > 200) {
            throw new BaseException(BookErrorCode.TOO_MANY_EXCEL_DATA, "데이터가 담긴 행이 200개 이하만 가능합니다,");
        }

        // title, author, publisher, category, level, isActive
        List<String>[] rows = ExcelUtil.extractCellData(workbook, 6);

        List<BookCreateReq> bookCreateReqList = toBookCreateReqList(rows);

        for (BookCreateReq bookCreateReq : bookCreateReqList) {
            System.out.println(bookCreateReq);
        }
    }


    private List<BookCreateReq> toBookCreateReqList(List<String>[] rows) {

        List<BookCreateReq> bookCreateReqList = new ArrayList<>();

        for (List<String> row : rows) {
            ExcelBookDto excelBookDto = toExcelBookDto(row);

            dtoValidator.validate(excelBookDto);

            bookCreateReqList.add(excelBookDto.toBookCreateReq());
        }

        return bookCreateReqList;
    }


    private ExcelBookDto toExcelBookDto(List<String> row) {

        ExcelBookDto excelBookDto = new ExcelBookDto();
        excelBookDto.setTitle(row.get(0));
        excelBookDto.setAuthor(row.get(1));
        excelBookDto.setPublisher(row.get(2));
        excelBookDto.setCategory(row.get(3));
        excelBookDto.setLevel(row.get(4));
        excelBookDto.setIsActive(row.get(5));

        return excelBookDto;
    }
}
