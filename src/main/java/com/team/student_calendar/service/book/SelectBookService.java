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

import java.util.ArrayList;
import java.util.List;

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
     * 난이도/제목순 전체 책 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByDifficultyAscAndTitleAsc() {
        Sort sort = Sort.by(Sort.Direction.ASC, "difficulty").and(Sort.by(Sort.Direction.ASC, "title"));
        return bookRepository.findAll(sort);
    }

    /**
     * 슬라이더 도서 추출 (현재 읽어야 할 책이 중앙에 오도록 5권 자르기)
     */
    @Transactional(readOnly = true)
    public List<BookSliderRes> getSliderBooks(Long studentId) {

        List<BookEntity> allBooks = findAllByDifficultyAscAndTitleAsc();

        if (allBooks.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        BookProgressEntity progress = bookProgressRepository.findByStudentEntity_Id(studentId).orElse(null);

        // 현재 읽는 책 인덱스 찾기
        int targetIndex = 0; // 진도가 없으면 무조건 1번째 책

        if (progress != null && progress.getBookEntity() != null) {
            Long currentBookId = progress.getBookEntity().getId();

            for (int i = 0; i < allBooks.size(); i++) {
                if (allBooks.get(i).getId().equals(currentBookId)) {
                    targetIndex = i; // 발견
                    break;
                }
            }
        }

        // 5권 자르기 알고리즘
        int startIndex = Math.max(0, targetIndex - 2);
        int endIndex = Math.min(allBooks.size(), startIndex + 5);
        if (endIndex - startIndex < 5 && allBooks.size() >= 5) {
            startIndex = endIndex - 5;
        }

        List<BookSliderRes> result = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            BookEntity book = allBooks.get(i);

            // 타겟보다 앞에 있으면 무조건 이미 읽은 책(true)
            boolean isRead = (i < targetIndex);

            result.add(new BookSliderRes(
                    book.getBookNo(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getImageUrl(),
                    isRead
            ));
        }

        return result;
    }
}