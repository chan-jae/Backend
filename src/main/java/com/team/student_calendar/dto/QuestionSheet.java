package com.team.student_calendar.dto;

import java.util.List;

/**
 * 독서 질문지 (Claude 응답 JSON <-> PDF 템플릿 변수)
 * answer는 모두 1부터 시작하는 번호 (①=1)
 */
public record QuestionSheet(
        List<VocabularyQuiz> vocabulary,            // 어휘 5개
        List<MultipleChoice> factual,               // 사실적 이해 4지 선다 3개
        List<ShortAnswer> factualShortAnswer,       // 사실적 이해 주관식 3개
        List<MultipleChoice> critical,              // 비판적 이해 4지 선다 3개
        List<MultipleChoice> appreciative,          // 감상적 이해 4지 선다 3개
        List<MultipleChoice> creative               // 창의적 이해 4지 선다 3개
) {

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
