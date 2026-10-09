package com.team.student_calendar.service.isbnlookup;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.BetaFallbacksParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.BetaTextBlock;
import com.anthropic.models.beta.messages.BetaWebFetchTool20260209;
import com.anthropic.models.beta.messages.BetaWebSearchTool20260209;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.enums.BookType;
import com.team.student_calendar.common.enums.IsbnLookupError;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.dto.IsbnBookRes;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.dto.QuestionSheetRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.IsbnLookupEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.IsbnLookupRepository;
import com.team.student_calendar.service.file.UploadFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertIsbnLookupService {

    private final BookRepository bookRepository;
    private final IsbnLookupRepository isbnLookupRepository;
    private final AnthropicClient anthropicClient;
    private final UploadFileService uploadFileService;
    private final TransactionTemplate transactionTemplate;

    private static final String YES24_URL = "https://apis.yes24.com/v1/goods/itemDetail?query={isbn}";
    private static final String NARU_URL = "https://data4library.kr/api/srchDtlList?authKey={key}&isbn13={isbn}";

    // 외부 API 호출은 블로킹 I/O라 가상 스레드로 실행 (공용 ForkJoinPool 고갈 방지)
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final RestClient restClient = createRestClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${yes24-token}")
    private String yes24Token;

    @Value("${naru-token}")
    private String naruToken;

    // 활동지 문제 생성 상태 (key: bookId), 프론트가 폴링으로 조회
    // 메모리 보관이라 재시작하면 사라지고 서버 1대 전제. 서버를 늘리거나 결과를 남겨야 하면 DB로
    private final Map<Long, QuestionSheetRes> questionSheets = new ConcurrentHashMap<>();

    /**
     * 활동지 문제 생성을 백그라운드로 시작하고 바로 반환 (결과는 findDoneQuestionSheets로 폴링)
     * 같은 책을 생성 중이면 에러 (API 비용 중복 방지)
     * @param bookId 문제를 만들 책 id
     */
    public void requestQuestionSheet(Long bookId) {

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BaseException(BookErrorCode.BOOK_NOT_FOUND));

        // ISBN으로 저장된 책 중 활동지 대기(C_PENDING) 상태만 생성 가능 (ISBN 없는 책은 조회 기록도 없음)
        IsbnLookupEntity lookup = isbnLookupRepository.findFirstByIsbnOrderByIdDesc(book.getIsbn())
                .orElseThrow(() -> new BaseException(BookErrorCode.ISBN_NOT_FOUND));

        if (lookup.getStatus() != IsbnLookupStatus.C_PENDING) {
            throw new BaseException(BookErrorCode.QUESTION_SHEET_NOT_ALLOWED);
        }

        // 확인과 C_PENDING 등록을 한 번에 (동시 요청이 둘 다 통과하지 않도록)
        questionSheets.compute(bookId, (id, prev) -> {
            if (prev != null && prev.status() == QuestionSheetRes.Status.PENDING) {
                throw new BaseException(BookErrorCode.QUESTION_SHEET_IN_PROGRESS);
            }
            return QuestionSheetRes.pending(lookup.getId());
        });

        // 백그라운드 예외는 GlobalExceptionHandler로 안 가므로 여기서 잡아 FAILED로 기록
        executor.submit(() -> {
            try {
                QuestionSheet questionSheet = createQuestionSheet(book);
                saveQuestionSheet(bookId, lookup, questionSheet);
                questionSheets.put(bookId, QuestionSheetRes.success(lookup.getId()));
            } catch (Exception e) {
                log.error("claude question sheet fail - bookId: {}", bookId, e);
                failQuestionSheet(bookId, lookup);
            }
        });
    }


    /**
     * 활동지 생성 실패 기록: 폴링 결과 FAILED + 조회 기록 FAILED(CLAUDE) 저장 (재요청 없음)
     * @param bookId 책 id
     * @param lookup 상태를 바꿀 조회 기록
     */
    void failQuestionSheet(Long bookId, IsbnLookupEntity lookup) {

        questionSheets.put(bookId, QuestionSheetRes.failed(lookup.getId()));

        // saveQuestionSheet 롤백돼도 메모리 객체엔 SUCCESS/문제가 남아 있으므로 같이 되돌림
        lookup.setQuestionSheet(null);
        lookup.setStatus(IsbnLookupStatus.FAILED);
        lookup.setError(IsbnLookupError.CLAUDE);
        try {
            isbnLookupRepository.save(lookup);
        } catch (Exception saveError) {
            // DB까지 안 되면 C_PENDING으로 남음, 로그로 확인
            log.error("question sheet FAILED save fail - lookupId: {}, error: {}", lookup.getId(), saveError.getMessage());
        }
    }


    /**
     * 조회 기록 SUCCESS 저장 + 활동지 PDF 업로드(file 저장 -> S3)를 한 트랜잭션으로 묶음
     * S3는 트랜잭션에 못 묶으므로 DB 작업을 먼저 다 하고 S3를 마지막에 -> S3 실패 시 전부 롤백 (이후 failQuestionSheet가 FAILED로 기록)
     * (가상 스레드에서 자기 메서드 호출이라 @Transactional 프록시를 안 타서 TransactionTemplate 사용)
     * 서버 시작 시 PDF 재업로드(StartupController)에서도 사용
     * @param bookId 책 id
     * @param lookup 상태를 바꿀 조회 기록
     * @param questionSheet Claude가 채운 활동지 문제
     */
    public void saveQuestionSheet(Long bookId, IsbnLookupEntity lookup, QuestionSheet questionSheet) {

        transactionTemplate.executeWithoutResult(status -> {
            lookup.setQuestionSheet(questionSheet);
            lookup.setStatus(IsbnLookupStatus.SUCCESS);
            lookup.setError(null);
            // 커밋 때가 아니라 지금 UPDATE를 날려서, DB 에러가 S3 업로드 전에 나도록
            isbnLookupRepository.saveAndFlush(lookup);
            uploadFileService.uploadMyBookPdf(bookId, questionSheet);
        });
    }


    /**
     * 생성이 끝난(SUCCESS/FAILED) 활동지 문제 전체 조회 (프론트 폴링용), 응답한 건 메모리에서 제거
     * @return 없으면 빈 List
     */
    public List<QuestionSheetRes> findDoneQuestionSheets() {

        List<QuestionSheetRes> res = new ArrayList<>();
        questionSheets.forEach((bookId, sheet) -> {
            // remove(key, value)는 값이 그대로일 때만 지움 -> 동시 폴링이 같은 결과를 두 번 받거나, 그 사이 새로 등록된 요청을 지우지 않음
            if (sheet.status() != QuestionSheetRes.Status.PENDING && questionSheets.remove(bookId, sheet)) {
                res.add(sheet);
            }
        });
        return res;
    }


    /**
     * 책 정보로 Claude에 활동지 문제 생성 요청 (md/ 프롬프트 + json/book-question-base.json 틀)
     * @param book 문제를 만들 책
     * @return Claude가 틀을 채운 활동지 문제
     */
    private QuestionSheet createQuestionSheet(BookEntity book) {

        String userMessage = readResource("md/book-question-user.md")
                .replace("{bookTitle}", book.getTitle())
                .replace("{author}", String.valueOf(book.getAuthor()))
                .replace("{publisher}", String.valueOf(book.getPublisher()))
                .replace("{level}", String.valueOf(book.getLevel()))
                .replace("{difficulty}", String.valueOf(book.getDifficulty()))
                .replace("{template}", readResource("json/book-question-base.json"));

        MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-opus-5-5")
                .maxTokens(16000L)
                // opus 5.5 기본 effort가 medium이라 명시
                .outputConfig(BetaOutputConfig.builder().effort(BetaOutputConfig.Effort.MEDIUM).build())
                // 안전 분류기가 거절하면 서버가 다른 모델로 자동 재시도
                .addBeta(AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01)
                .fallbacks(BetaFallbacksParam.ofDefault())
                // Claude가 모르는 책이 많아서 서점 소개, 서평, 블로그 리뷰를 검색하고 본문까지 읽게 함
                // 횟수 상한으로 비용만 막음 (검색 1회 약 $0.01 + 결과 토큰), 결과가 부족하면 maxUses 조정
                .addTool(BetaWebSearchTool20260209.builder().maxUses(5L).build())
                .addTool(BetaWebFetchTool20260209.builder().maxUses(5L).maxContentTokens(10000L).build())
                .system(readResource("md/book-question-system.md"))
                .addUserMessage(userMessage)
                .build();

        BetaMessage response = anthropicClient.beta().messages().create(params);

        // 서버 쪽 검색 반복이 길어지면 PAUSE_TURN으로 끊김 → 받은 응답을 그대로 붙여 다시 보내면 이어서 진행
        for (int i = 0; i < 3 && BetaStopReason.PAUSE_TURN.equals(response.stopReason().orElse(null)); i++) {
            params = params.toBuilder().addMessage(response).build();
            response = anthropicClient.beta().messages().create(params);
        }

        // 토큰 한도로 잘리거나 거절되면 JSON이 불완전함
        BetaStopReason stopReason = response.stopReason().orElse(null);
        if (!BetaStopReason.END_TURN.equals(stopReason)) {
            log.warn("claude question sheet fail - bookId: {}, stopReason: {}", book.getId(), stopReason);
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "문제 생성 실패: " + stopReason);
        }

        // 검색 중간에 "찾아볼게요" 같은 글이 섞이고 인용 때문에 text 블록이 쪼개지므로, 합친 뒤 { ~ } 만 잘라냄
        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(BetaTextBlock::text)
                .collect(Collectors.joining());
        int start = text.indexOf('{');
        String json = start < 0 ? text : text.substring(start, text.lastIndexOf('}') + 1);

        log.debug("claude question sheet complete - bookId: {}, usage: {}, json: {}", book.getId(), response.usage(), json);

        if (json.contains("UNKNOWN_BOOK")) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "웹에서 책 내용을 충분히 찾지 못해 문제를 만들지 않았습니다.");
        }

        try {
            return objectMapper.readValue(json, QuestionSheet.class);
        } catch (JacksonException e) {
            log.warn("claude question sheet parse fail - bookId: {}, json: {}", book.getId(), json);
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "문제 생성 결과가 JSON 틀과 다릅니다.");
        } catch (Exception e) {
            log.warn("claude question fail - bookId: {}, json: {}", book.getId(), json);
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "활동지 읽는 중 에러가 발생했습니다.");
        }
    }

    private String readResource(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


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
            throw new BaseException(BookErrorCode.ALREADY_EXIST_ISBN, "처리중인 항목이 있습니다.");
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
        lookup.setStatus(lookup.getError() == null ? IsbnLookupStatus.C_PENDING : IsbnLookupStatus.FAILED);



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

        BookEntity saved = bookRepository.save(bookEntity);

        log.info("isbn book save complete - id: {}, isbn: {}", bookEntity.getId(), isbn);

        lookup.setBook(saved);
        isbnLookupRepository.save(lookup);
        res = IsbnBookRes.from(lookup);

        log.info("isbn lookup complete - isbn: {}, status: {}, error: {}", isbn, lookup.getStatus(), lookup.getError());

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
