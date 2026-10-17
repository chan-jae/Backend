package com.team.student_calendar.dto;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.entity.BookEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 독서 질문지 (Claude 응답 JSON <-> PDF 템플릿 변수)
 * answer는 모두 1부터 시작하는 번호 (①=1)
 * id는 활동지 전체에서 문제를 구분하는 번호 (json/book-question-base.json 틀에 고정, 문제 일부 재생성 때 사용)
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

    /** 활동지에 있는 문제 id 전체 (id 없는 문제는 제외) */
    public Set<Integer> ids() {
        return Stream.<List<? extends Question>>of(vocabulary, factual, factualShortAnswer, critical, appreciative, creative)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Question::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * generated에서 ids에 해당하는 문제만 꺼내 이 활동지의 같은 id 자리에 끼워 넣음 (나머지 문제는 그대로)
     * Claude가 요청하지 않은 문제를 바꿔서 응답해도 무시됨
     * @param generated Claude가 새로 만든 활동지 (전체)
     * @param ids 바꿀 문제 id
     * @return 문제를 바꾼 활동지 (generated에 해당 id가 없으면 에러)
     */
    public QuestionSheet replaceByIds(QuestionSheet generated, Set<Integer> ids) {
        return new QuestionSheet(book,
                replace(vocabulary, generated.vocabulary, ids),
                replace(factual, generated.factual, ids),
                replace(factualShortAnswer, generated.factualShortAnswer, ids),
                replace(critical, generated.critical, ids),
                replace(appreciative, generated.appreciative, ids),
                replace(creative, generated.creative, ids));
    }

    private static <T extends Question> List<T> replace(List<T> current, List<T> generated, Set<Integer> ids) {

        Map<Integer, T> generatedById = generated == null ? Map.of() : generated.stream()
                .filter(q -> q.id() != null)
                .collect(Collectors.toMap(Question::id, Function.identity(), (a, b) -> a));

        return current.stream().map(q -> {
            if (!ids.contains(q.id())) {
                return q;
            }
            T replaced = generatedById.get(q.id());
            if (replaced == null) {
                throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "새로 만든 문제에 id " + q.id() + "가 없습니다.");
            }
            return replaced;
        }).toList();
    }

    private static <T> List<T> first(List<T> list, int count) {
        return list.subList(0, Math.min(count, list.size()));
    }

    public record BookInfo(String title, String author, String publisher, String level, Integer difficulty) {}

    /** 모든 문제 공통: 활동지 전체에서 문제를 구분하는 id */
    public interface Question {
        Integer id();
    }

    /**
     * @param id 문제 id
     * @param sentence 예문 (word가 그대로 포함돼야 밑줄 표시됨)
     * @param word 밑줄 칠 어휘~
     * @param meanings 뜻 보기 (O/X 표시용)
     * @param answer 맞는 뜻 번호
     */
    public record VocabularyQuiz(Integer id, String sentence, String word, List<String> meanings, int answer) implements Question {}

    public record MultipleChoice(Integer id, String question, List<String> choices, int answer) implements Question {}

    public record ShortAnswer(Integer id, String question, String exampleAnswer) implements Question {}
}
