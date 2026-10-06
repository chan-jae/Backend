package com.team.student_calendar.service.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.team.student_calendar.common.constant.BookLevelMapping;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.common.util.DtoValidator;
import com.team.student_calendar.common.util.ExcelUtil;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.dto.ExcelBookReq;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.jdbc.BookJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookRepository bookRepository;
    private final BookJdbcRepository bookJdbcRepository;
    private final DtoValidator dtoValidator;
    private final ValidateBookDupService validateBookDupService;

    private static final String YES24_URL = "https://apis.yes24.com/v1/goods/itemDetail?query={isbn}";
    private static final String NARU_URL = "https://data4library.kr/api/srchDtlList?authKey={key}&isbn13={isbn}";

    // 외부 API 호출은 블로킹 I/O라 가상 스레드로 실행 (공용 ForkJoinPool 고갈 방지)
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final RestClient restClient = createRestClient();

    @Value("${yes24-token}")
    private String yes24Token;

    @Value("${naru-token}")
    private String naruToken;


    /**
     * 책 list 저장
     * @param bookList 책 list
     * @return 삽입,갱신,스킵 건수
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
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

        // 필드 검증
        req.validate();

        // 등록된 책인지 체크
        validateBookDupService.checkBookDuplication(req.getTitle(), req.getAuthor());

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
                .bHash(BookHashUtil.generateBookHashKey(req.getTitle(), req.getAuthor()))
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
    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public UpsertResult saveBookByExcel(MultipartFile file) {

        log.info("file name: {}", file.getOriginalFilename());

        Workbook workbook = ExcelUtil.convertToWorkbook(file);

        // 실제 작성된 행이 200건 이하만 통과
        if (workbook.getSheetAt(0).getPhysicalNumberOfRows() > 200) {
            throw new BaseException(BookErrorCode.TOO_MANY_EXCEL_DATA, "데이터가 담긴 행이 200개 이하만 가능합니다,");
        }

        // title, author, publisher, category, level, isActive
        List<String>[] rows = ExcelUtil.extractCellData(workbook, 6);

        List<BookCreateReq> bookCreateReqList = toBookCreateReqList(rows);

//        for (BookCreateReq bookCreateReq : bookCreateReqList) {
//            System.out.println(bookCreateReq);
//        }

        UpsertResult result = bookJdbcRepository.bulkInsertBooksByExcel(bookCreateReqList);

        log.info("book save complete — inserted: {}", result.insertedCount());

        return result;
    }


    /**
     * ISBN으로 YES24, 정보나루 API를 병렬 호출해 책 저장
     * @param isbn ISBN13
     */
    public void saveBookByIsbn(String isbn) {

        // 두 API를 병렬·비동기로 호출하고, 둘 다 끝날 때까지 대기
        CompletableFuture<Yes24Item> yes24Future = CompletableFuture.supplyAsync(() -> fetchYes24(isbn), executor);
        CompletableFuture<String> classNoFuture = CompletableFuture.supplyAsync(() -> fetchClassNo(isbn), executor);
        CompletableFuture.allOf(yes24Future, classNoFuture).join();

        Yes24Item item = yes24Future.join();
        String classNo = classNoFuture.join();

        if (item != null) {
            System.out.println("title = " + item.title());
            System.out.println("author = " + item.author());
            System.out.println("publisher = " + item.publisher());
            System.out.println("isbn13 = " + item.isbn13());
            System.out.println("cover = " + item.cover());
        }
        System.out.println("class_no = " + classNo);
    }






    private List<BookCreateReq> toBookCreateReqList(List<String>[] rows) {

        List<BookCreateReq> bookCreateReqList = new ArrayList<>();

        int i = 1;
        for (List<String> row : rows) {
            ExcelBookReq excelBookReq = toExcelBookReq(row);

            // 필드 검증
            dtoValidator.validate(excelBookReq, i);

            // 등록된 책인지 검증
            try {
                validateBookDupService.checkBookDuplication(excelBookReq.getTitle(), excelBookReq.getAuthor());
            } catch (BaseException e) {
                String s = String.format("%d행에 중복된 책이 있습니다.", i);
                throw new BaseException(BookErrorCode.ALREADY_EXIST_BOOK, s);
            } catch (Exception e) {
                throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            bookCreateReqList.add(excelBookReq.toBookCreateReq());
            i++;
        }

        return bookCreateReqList;
    }








    private ExcelBookReq toExcelBookReq(List<String> row) {

        ExcelBookReq excelBookReq = new ExcelBookReq();
        excelBookReq.setTitle(row.get(0));
        excelBookReq.setAuthor(row.get(1));
        excelBookReq.setPublisher(row.get(2));
        excelBookReq.setCategory(row.get(3));
        excelBookReq.setLevel(row.get(4));
        excelBookReq.setIsActive(row.get(5));

        return excelBookReq;
    }


    private static RestClient createRestClient() {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder().requestFactory(requestFactory).build();
    }


    private Yes24Item fetchYes24(String isbn) {
        try {
            ResponseEntity<Yes24Response> res = restClient.get()
                    .uri(YES24_URL, isbn)
                    .header("X-Api-Key", yes24Token)
                    .retrieve()
                    .toEntity(Yes24Response.class);

            Yes24Response body = res.getBody();
            if (res.getStatusCode() != HttpStatus.OK || body == null || !body.success()
                    || body.data() == null || body.data().items() == null || body.data().items().isEmpty()) {
                log.info("yes24 api fail isbn={}, status={}", isbn, res.getStatusCode());
                return null;
            }
            return body.data().items().getFirst();
        } catch (Exception e) {
            log.warn("yes24 api error isbn={}", isbn, e);
            return null;
        }
    }


    private String fetchClassNo(String isbn) {
        try {
            byte[] xml = restClient.get()
                    .uri(NARU_URL, naruToken, isbn)
                    .retrieve()
                    .body(byte[].class);
            if (xml == null) {
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 외부 응답이므로 XXE 차단
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));

            NodeList nodes = doc.getElementsByTagName("class_no");
            return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent().strip();
        } catch (Exception e) {
            log.warn("naru api error isbn={}", isbn, e);
            return null;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record Yes24Response(boolean success, Yes24Data data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Yes24Data(List<Yes24Item> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Yes24Item(String title, String author, String publisher, String isbn13, String cover) {}
}
