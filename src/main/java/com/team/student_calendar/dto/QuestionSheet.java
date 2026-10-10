package com.team.student_calendar.dto;

import com.team.student_calendar.entity.BookEntity;

import java.util.List;

/**
 * 독서 질문지 (Claude 응답 JSON <-> PDF 템플릿 변수)
 * answer는 모두 1부터 시작하는 번호 (①=1)
 */
public record QuestionSheet(
        BookInfo book,                              // 문제 생성에 쓴 책 정보 (Claude 응답엔 없고 DB 저장할 때만 채움)
        List<VocabularyQuiz> vocabulary,            // 어휘 5개
        List<MultipleChoice> factual,               // 사실적 이해 4지 선다 6개 (저학년 PDF엔 앞 3개만)
        List<ShortAnswer> factualShortAnswer,       // 사실적 이해 단답형 3개 (저학년 PDF엔 앞 1개만)
        List<MultipleChoice> critical,              // 비판적 이해 4지 선다 2개
        List<MultipleChoice> appreciative,          // 감상적 이해 4지 선다 2개
        List<MultipleChoice> creative               // 창의적 이해 4지 선다 2개
) {

    public QuestionSheet withBook(BookEntity entity) {
        BookInfo info = new BookInfo(entity.getTitle(), entity.getAuthor(), entity.getPublisher(), entity.getLevel(), entity.getDifficulty());
        return new QuestionSheet(info, vocabulary, factual, factualShortAnswer, critical, appreciative, creative);
    }

    /** 저학년 PDF용: 사실적 이해 객관식 앞 3개, 단답형 앞 1개만 (DB에 저장된 활동지는 그대로) */
    public QuestionSheet forLowerGrade() {
        return new QuestionSheet(book, vocabulary, first(factual, 3), first(factualShortAnswer, 1), critical, appreciative, creative);
    }

    private static <T> List<T> first(List<T> list, int count) {
        return list.subList(0, Math.min(count, list.size()));
    }

    public record BookInfo(String title, String author, String publisher, String level, Integer difficulty) {}

    /**
     * @param sentence 예문 (word가 그대로 포함돼야 밑줄 표시됨)
     * @param word 밑줄 칠 어휘~
     * @param meanings 뜻 보기 (O/X 표시용)
     * @param answer 맞는 뜻 번호
     */
    public record VocabularyQuiz(String sentence, String word, List<String> meanings, int answer) {}

    public record MultipleChoice(String question, List<String> choices, int answer) {}

    public record ShortAnswer(String question, String exampleAnswer) {}
}
