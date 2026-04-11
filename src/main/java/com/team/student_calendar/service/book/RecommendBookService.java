package com.team.student_calendar.service.book;

import com.team.student_calendar.common.enums.StudentBookState;
import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.BookRepository;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendBookService {

    private final BookRepository bookRepository;
    private final SelectStudentService selectStudentService;
    private final StudentBookRepository studentBookRepository;

    /**
     * 책 추천 기능 (이전, 현재, 다음)
     */
    @Transactional(readOnly = true)
    public List<BookSliderRes> getRecommendSliderBooks(Long studentId) {
        StudentEntity student = selectStudentService.findById(studentId);
        String initialLevel = student.getFirstLevel();

        // 이전 책
        StudentBookEntity progress = studentBookRepository.findByStudentIdAndState(
                studentId, (byte) StudentBookState.DONE.getState(),
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("readAt"), Sort.Order.desc("id")))
        ).getContent().stream().findFirst().orElse(null);

        BookEntity previousBook = (progress != null) ? progress.getBook() : null;

        // 이전 책 문학이었는지 확인 + 처음 온 학생은 문학부터
        boolean wasLiterature = (previousBook != null) && "LITERATURE".equals(previousBook.getCategory());

        // 독서기록 가져오고 읽은 책 0권이면 -1L 하나 넣기
        List<Long> readBookIds = studentBookRepository.findAllByStudentId(studentId).stream()
                .map(sb -> sb.getBook().getId())
                .collect(Collectors.toList());
        if (readBookIds.isEmpty()) readBookIds.add(-1L);

        // 읽고 있는 책
        List<Byte> inProgressStates = Arrays.asList(
                (byte) StudentBookState.READING.getState(),
                (byte) StudentBookState.WRITING.getState()
        );

        StudentBookEntity currentProgress = studentBookRepository.findByStudentIdAndStateIn(
                studentId,
                inProgressStates,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent().stream().findFirst().orElse(null);

        BookEntity currentBook;
        boolean isCurrentLiterature;

        if (currentProgress != null) {
            // (READING)이나 (WRITING)인 책이 있으면 고정
            currentBook = currentProgress.getBook();
            isCurrentLiterature = "LITERATURE".equals(currentBook.getCategory());
        } else {
            // 진행 중인 책이 없으면 다음 추천 책을 가져옴
            isCurrentLiterature = !wasLiterature;
            currentBook = findNextBookWithAutoLevelUp(initialLevel, isCurrentLiterature, readBookIds);
        }

        // 현재 책 중복 방지로 미리 추가
        if (currentBook != null && !readBookIds.contains(currentBook.getId())) readBookIds.add(currentBook.getId());

        // 다음
        boolean isNextLiterature = !isCurrentLiterature;
        BookEntity nextBook = findNextBookWithAutoLevelUp(initialLevel, isNextLiterature, readBookIds);

        return Arrays.asList(
                convertToDto(previousBook, true),
                convertToDto(currentBook,    false),
                convertToDto(nextBook, false)
        );
    }

    // 레벨업 고려한 다음 책
    private BookEntity findNextBookWithAutoLevelUp(String initialLevel, boolean isLiterature, List<Long> readBookIds) {
        // 탐색 시작 레벨 설정
        String targetLevel = isLiterature ? initialLevel : calculateNonLiteratureLevel(initialLevel);

        while (targetLevel != null) {
            // 현재 타겟 레벨에서 안 읽은 책이 있는지 확인
            BookEntity foundBook = getSingleTargetBook(isLiterature, targetLevel, readBookIds);

            if (foundBook != null) {
                return foundBook; // 책을 찾으면 즉시 반환하고 종료
            }

            // 해당 레벨에 책이 없으면 다음 레벨을 가져옴
            // 다음 레벨로 넘어가는 경우는 initialLevel도 같이 올려줘야되나?
            String nextLevel = getNextLevelInOrder(targetLevel);

            // 무한루프 예방
            if (nextLevel == null || nextLevel.equals(targetLevel)) {
                break;
            }

            targetLevel = nextLevel;
        }

        return null; // 전 레벨을 다 뒤졌는데도 읽을 책이 하나도 없는 경우
    }

    // 이전 책이 문학인지 비문학인지
    private StudentBookEntity getLatestReadBook(Long studentId, boolean isLiterature) {
        if (isLiterature) {
            return studentBookRepository.findTopByStudentIdAndBook_CategoryAndStateOrderByReadAtDesc(
                    studentId, "LITERATURE", (byte) StudentBookState.DONE.getState()).orElse(null);
        } else {
            return studentBookRepository.findTopByStudentIdAndBook_CategoryNotAndStateOrderByReadAtDesc(
                    studentId, "LITERATURE", (byte) StudentBookState.DONE.getState()).orElse(null);
        }
    }

    private BookEntity getSingleTargetBook(boolean isLiterature, String targetLevel, List<Long> readBookIds) {
        if (isLiterature) {
            return bookRepository.findTop1ByCategoryAndLevelAndIdNotInOrderByDifficultyAsc(
                    "LITERATURE", targetLevel, readBookIds).orElse(null);
        } else {
            return bookRepository.findTop1ByCategoryNotAndLevelAndIdNotInOrderByDifficultyAsc(
                    "LITERATURE", targetLevel, readBookIds).orElse(null);
        }
    }

    // 비분학 레벨 초기 설정
    private String calculateNonLiteratureLevel(String initialLevel) {
        if (initialLevel == null || !initialLevel.contains("_")) return "A_0";
        String[] parts = initialLevel.split("_");
        try {
            int number = Integer.parseInt(parts[1]);
            return number <= 2 ? "A_0" : "A_" + (number - 1);
        } catch (NumberFormatException e) { return "A_0"; }
    }

    // 다음 레벨 변경 로직
    private String getNextLevelInOrder(String currentLevel) {
        if (currentLevel == null || !currentLevel.contains("_")) return currentLevel;
        String[] parts = currentLevel.split("_");
        if (parts.length != 2) return currentLevel;

        String prefix = parts[0];
        try {
            int number = Integer.parseInt(parts[1]);
            if ("A".equals(prefix)) return "B_" + number;
            if ("B".equals(prefix)) return "A_" + (number + 1);
        } catch (NumberFormatException ignored) {}

        return currentLevel;
    }

    /**
     * Entity -> DTO
     */
    private BookSliderRes convertToDto(BookEntity book, boolean realStatus) {
        if (book == null) return null;
        return new BookSliderRes(
                book.getId(),book.getBookNo(), book.getTitle(), book.getAuthor(),
                book.getCategory(), book.getImageUrl(), realStatus
        );
    }
}
