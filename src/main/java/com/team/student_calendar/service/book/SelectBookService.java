package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.BookProgressEntity;
import com.team.student_calendar.repository.BookProgressRepository;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelectBookService {

    private final BookRepository bookRepository;
    private final BookProgressRepository bookProgressRepository;

    /**
     * List안에 있는 bookNo를 가지는 BookEntity 가져오기
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByBookNoList(List<Long> bookNoList) {
        return bookRepository.findAllByBookNoIn(bookNoList);
    }

    /**
     * 책 목록 페이징
     */
    @Transactional(readOnly = true)
    public Page<BookEntity> getBookListWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "bookNo"));
        return bookRepository.findAll(pageable);
    }

    /**
     * 슬라이더 도서 추출 (현재 읽어야 할 책이 중앙에 오도록 5권 자르기)
     */
    @Transactional(readOnly = true)
    public List<BookSliderRes> getSliderBooks(Long studentId) {

        // 전체 책 목록
        List<BookEntity> allBooks = findAllByDifficultyAscAndTitleAsc();

        if (allBooks.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 학생 진도 찾기
        BookProgressEntity progress = bookProgressRepository.findByStudentId(studentId).orElse(null);

        int currentIndex = 0;

        // 전체 목록에서 현재 책 index 찾기
        if (progress != null && progress.getBook() != null) {
            Long currentBookId = progress.getBook().getId();
            for (int i = 0; i < allBooks.size(); i++) {
                if (allBooks.get(i).getId().equals(currentBookId)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        // 5권 시작점, 끝점 계산
        int startIndex = Math.max(0, currentIndex - 2);
        int endIndex = Math.min(allBooks.size() - 1, startIndex + 4);

        if (endIndex - startIndex < 4) {
            startIndex = Math.max(0, endIndex - 4);
        }

        // 5권을 잘라서 BookSliderRes로
        return allBooks.subList(startIndex, endIndex + 1).stream()
                .map(book -> new BookSliderRes(
                        book.getBookNo(),    // 1. 책 번호 (엔티티에 getId()로 되어있다면 book.getId()로 변경!)
                        book.getTitle(),     // 2. 제목
                        book.getAuthor(),    // 3. 작가
                        book.getCategory(),  // 4. 카테고리
                        book.getImageUrl(),  // 5. 이미지 URL
                        false                // 6. 읽음 여부 (슬라이더에 띄울 책이니 일단 false(안읽음) 처리)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 난이도, 제목순으로 BookEntity 가져오기
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByDifficultyAscAndTitleAsc() {
        Sort sort = Sort.by(
                Sort.Order.asc("difficulty"),
                Sort.Order.asc("title")
        );
        return bookRepository.findAll(sort);
    }
}