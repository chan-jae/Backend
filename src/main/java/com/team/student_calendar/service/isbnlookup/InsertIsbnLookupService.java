package com.team.student_calendar.service.isbnlookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.IsbnLookupError;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.dto.IsbnBookRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.IsbnLookupEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.IsbnLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertIsbnLookupService {

    private final BookRepository bookRepository;
    private final IsbnLookupRepository isbnLookupRepository;

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
     * ISBN을 PENDING 상태로 등록 (로그 테이블이라 매번 새 행 추가)
     * 이미 책으로 등록됐거나, FAILED가 아닌 조회 기록이 있으면 에러
     * @param isbn ISBN13
     * @return 저장된 조회 기록 id
     */
    @Transactional
    public Long saveIsbnPending(String isbn) {

        if (bookRepository.existsByIsbn(isbn)) {
            throw new BaseException(BookErrorCode.ALREADY_EXIST_BOOK);
        }

        if (isbnLookupRepository.existsByIsbnAndStatusNot(isbn, IsbnLookupStatus.FAILED)) {
            throw new BaseException(BookErrorCode.ALREADY_EXIST_ISBN);
        }

        IsbnLookupEntity lookup = new IsbnLookupEntity();
        lookup.setIsbn(isbn);
        lookup.setStatus(IsbnLookupStatus.PENDING);
        isbnLookupRepository.save(lookup);

        log.info("isbn pending saved - id: {}, isbn: {}", lookup.getId(), isbn);

        return lookup.getId();
    }


    /**
     * 조회 기록 id의 ISBN으로 YES24, 정보나루 API를 병렬 호출해 조회 결과 저장 (이미 책으로 등록된 ISBN이면 에러)
     * 조회 성공 시 BookEntity에도 저장, 실패 시 로그만 FAILED로 남김
     * 외부 API 대기 중 DB 커넥션을 잡지 않도록 @Transactional 없이 save()별로 커밋
     * @param id saveIsbnPending에서 반환한 조회 기록 id
     * @return 조회 결과 (조회 실패 시 status=FAILED, 조회된 값만 채움)
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
    public IsbnBookRes saveBookByIsbn(Long id) {

        IsbnLookupEntity lookup = isbnLookupRepository.findById(id)
                .orElseThrow(() -> new BaseException(BookErrorCode.ISBN_NOT_FOUND));
        String isbn = lookup.getIsbn();

        // 이미 책으로 등록된 ISBN이면 API 요청 불가
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BaseException(BookErrorCode.ALREADY_EXIST_BOOK);
        }

        // 두 API를 병렬·비동기로 호출하고, 둘 다 끝날 때까지 대기
        CompletableFuture<Yes24Item> yes24Future = CompletableFuture.supplyAsync(() -> fetchYes24(isbn), executor);
        CompletableFuture<String> classNoFuture = CompletableFuture.supplyAsync(() -> fetchClassNo(isbn), executor);
        CompletableFuture.allOf(yes24Future, classNoFuture).join();

        Yes24Item item = yes24Future.join();
        String classNo = classNoFuture.join();

        if (item != null) {
            lookup.setTitle(item.title());
            lookup.setAuthor(item.author());
            lookup.setPublisher(item.publisher());
            lookup.setImageUrl(item.cover());

        }
        if (classNo != null) {
            lookup.setClassNo(classNo);
            // KDC 분류번호 8xx = 문학
            lookup.setCategory(classNo.startsWith("8")
                    ? BookCategory.LITERATURE.name()
                    : BookCategory.NON_LITERATURE.name());
        }

        // 둘 다 실패하면 책 정보 자체가 없는 YES24를 우선 기록
        if (item == null) {
            lookup.setError(IsbnLookupError.YES24);
        } else if (classNo == null) {
            lookup.setError(IsbnLookupError.NARU);
        }
        lookup.setStatus(lookup.getError() == null ? IsbnLookupStatus.SUCCESS : IsbnLookupStatus.FAILED);

        isbnLookupRepository.save(lookup);

        log.info("isbn lookup complete - isbn: {}, status: {}, error: {}", isbn, lookup.getStatus(), lookup.getError());

        // 실패해도 조회된 값까지는 응답 (YES24 실패 시 title/author/publisher, 정보나루 실패 시 category가 null)
        IsbnBookRes res = IsbnBookRes.from(lookup);

        if (lookup.getStatus() == IsbnLookupStatus.FAILED) {
            return res;
        }

        // 레벨은 나중에 직접 지정하므로 cLevel 0으로 저장
        BookEntity bookEntity = BookEntity.builder()
                .title(item.title())
                .author(item.author())
                .publisher(item.publisher())
                .category(lookup.getCategory())
                .difficulty(0)
                .cLevel((byte) 0)
                .bookNo(null)
                .imageUrl(item.cover())
                .type(BookType.CUSTOM.getType())
                .isActive((byte) 1)
                .bHash(BookHashUtil.generateBookHashKey(item.title(), item.author()))
                .isbn(isbn)
                .updatedAt(LocalDateTime.now())
                .build();

        bookRepository.save(bookEntity);

        log.info("isbn book save complete - id: {}, isbn: {}", bookEntity.getId(), isbn);

        return res;
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
            log.warn("yes24 api error isbn={}, msg={}", isbn, e.getMessage());
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
            log.warn("naru api error isbn={}, msg={}", isbn, e.getMessage());
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
