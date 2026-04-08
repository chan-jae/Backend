package com.team.student_calendar.service.book;

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
        String studentLevel = student.getFirstLevel();

        // 이전 책
        StudentBookEntity progress = studentBookRepository.findByStudentIdAndState(
                studentId, (byte) 1,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent().stream().findFirst().orElse(null);

        BookEntity previousBook = (progress != null) ? progress.getBook() : null;

        // 이전 책 문학이었는지 확인 + 처음 온 학생은 문학부터
        boolean wasLiterature = (previousBook != null) && "LITERATURE".equals(previousBook.getCategory());

        // 독서기록 가져오고 읽은 책 0권이면 -1L 하나 넣기
        List<Long> readBookIds = studentBookRepository.findAllByStudentId(studentId).stream()
                .map(sb -> sb.getBook().getId())
                .collect(Collectors.toList());
        if (readBookIds.isEmpty()) readBookIds.add(-1L);

        StudentBookEntity readingProgress = studentBookRepository.findByStudentIdAndState(
                studentId, (byte) 0, // 👈 state 0(읽는 중)인 것을 찾습니다.
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent().stream().findFirst().orElse(null);

        BookEntity currentBook;
        boolean isCurrentLiterature;

        if (readingProgress != null) {
            // 읽고 있는 책이 있으면 그 책을 현재 자리에 고정
            currentBook = readingProgress.getBook();
            isCurrentLiterature = "LITERATURE".equals(currentBook.getCategory());
        } else {
            // 읽고 있는 책이 없으면 다음책 가져옴
            isCurrentLiterature = !wasLiterature; // 장르 교차 적용
            String currentLevel = calculateTargetLevel(studentLevel, isCurrentLiterature);
            currentBook = getSingleTargetBook(isCurrentLiterature, currentLevel, readBookIds);
        }

        // 현재 책 중복 방지로 미리 추가
        if (currentBook != null) readBookIds.add(currentBook.getId());

        // 다음
        boolean isNextLiterature = !isCurrentLiterature; // 다시 장르 교차!
        String nextLevel = calculateTargetLevel(studentLevel, isNextLiterature);
        BookEntity nextBook = getSingleTargetBook(isNextLiterature, nextLevel, readBookIds);

        return Arrays.asList(
                convertToDto(previousBook, true),
                convertToDto(currentBook, false),
                convertToDto(nextBook, false)
        );
    }

    /**
     * 문학/비문학 맞춰서 호출
     */
    private BookEntity getSingleTargetBook(boolean isLiterature, String targetLevel, List<Long> readBookIds) {
        if (isLiterature) {
            return bookRepository.findTop1ByCategoryAndLevelAndIdNotInOrderByDifficultyAsc(
                    "LITERATURE", targetLevel, readBookIds).orElse(null);
        } else {
            return bookRepository.findTop1ByCategoryNotAndLevelAndIdNotInOrderByDifficultyAsc(
                    "LITERATURE", targetLevel, readBookIds).orElse(null);
        }
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

    /**
     * 문학/비문학 레벨 선택 로직
     */
    private String calculateTargetLevel(String studentLevel, boolean isLiterature) {
        if (isLiterature) {
            return studentLevel;
        }

        String[] parts = studentLevel.split("_");

        int number = Integer.parseInt(parts[1]);

        // 2B 이하 레벨
        if (number <= 2) {
            return "A_0";
        }

        // 3A 이상
        return "A_" + (number - 1);
    }
}
