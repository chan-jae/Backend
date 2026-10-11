package com.team.student_calendar.service.file.util;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.entity.BookEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PdfRenderUtil {

    private static final String FONT_FAMILY = "NanumGothic";
    private static final String BRANCH_NAME = "리딩오션 용천점";   // 표지 왼쪽 위 지점명
    private static final int UPPER_GRADE_DIFFICULTY = 610;        // 도서난이도 이 값 이상이면 고학년 활동지 (미만은 저학년)

    private final TemplateEngine templateEngine;


    /**
     * Thymeleaf 템플릿을 렌더링해 PDF 바이트로 변환
     * @param template templates/ 기준 경로 (예: "pdf/question-sheet")
     * @param variables 템플릿 변수
     * @return PDF 바이트
     */
    public byte[] render(String template, Map<String, Object> variables) {

        String html = templateEngine.process(template, new Context(Locale.KOREAN, variables));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 한글 폰트를 직접 등록하지 않으면 한글이 모두 빠짐
            new PdfRendererBuilder()
                    .useFont(() -> font("NanumGothic-Regular.ttf"), FONT_FAMILY, 400, FontStyle.NORMAL, true)
                    .useFont(() -> font("NanumGothic-Bold.ttf"), FONT_FAMILY, 700, FontStyle.NORMAL, true)
                    // 보기 번호 ①②③④ 전용 (나눔고딕에 원문자 글리프 없음)
                    .useFont(() -> font("IBMPlexSansKR-Regular.ttf"), "IBMPlexSansKR", 400, FontStyle.NORMAL, true)
                    .withHtmlContent(html, null)
                    .toStream(out)
                    .run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * 책 정보 + 활동지 문제로 문제지 PDF 생성 (표지 포함, 정답 제외)
     * @param book 표지에 들어갈 책
     * @param sheet Claude가 채운 활동지 문제
     * @return PDF 바이트
     */
    public byte[] renderQuestionSheet(BookEntity book, QuestionSheet sheet) {
        return render("pdf/book-question-sheet", questionSheetVariables(book, sheet));
    }


    /**
     * 활동지 문제의 정답지 PDF 생성
     * @param book 제목 표시용 책
     * @param sheet Claude가 채운 활동지 문제
     * @return PDF 바이트
     */
    public byte[] renderQuestionSheetAnswer(BookEntity book, QuestionSheet sheet) {
        return render("pdf/book-question-sheet-answer", questionSheetVariables(book, sheet));
    }


    private static Map<String, Object> questionSheetVariables(BookEntity book, QuestionSheet sheet) {
        // 책마다 비어 있는 값이 있을 수 있어 null 허용 Map (Map.of는 null이면 NPE)
        Map<String, Object> variables = new HashMap<>();
        variables.put("bookTitle", book.getTitle());
        variables.put("author", book.getAuthor());
        variables.put("publisher", book.getPublisher());
        variables.put("difficulty", book.getDifficulty());
        variables.put("imageUrl", book.getImageUrl());
        variables.put("level", book.getLevel());
        variables.put("branchName", BRANCH_NAME);
        // 문제는 고학년 기준으로 저장돼 있음 -> 저학년이면 일부만 (난이도를 바꾸고 PDF 재업로드하면 그때 기준으로 다시 나뉨)
        variables.put("sheet", book.getDifficulty() < UPPER_GRADE_DIFFICULTY ? sheet.forLowerGrade() : sheet);
        return variables;
    }


    private static InputStream font(String fileName) {
        return PdfRenderUtil.class.getResourceAsStream("/fonts/" + fileName);
    }
}
