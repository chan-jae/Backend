package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectBookService {

    private final BookRepository bookRepository;


    /**
     * List안에 있는 bookNo를 가지는 BookEntity 가져오기
     * */
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
    public List<BookSliderRes> getSliderBooks(Long studentId) {
        List<BookSliderRes> allBooks = bookRepository.findAllBooksWithReadStatus(studentId);

        if (allBooks.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 가장 앞의 안 읽은 책 인덱스 찾기
        int targetIndex = -1;
        for (int i = 0; i < allBooks.size(); i++) {
            if (!allBooks.get(i).isRead()) {
                targetIndex = i; // 타겟 발견!
                break;
            }
        }

        // 만약 책 다 읽었으면 가장 마지막 책을 타겟으로 슬라이더의 맨 끝부분 보여줌
        if (targetIndex == -1) {
            targetIndex = allBooks.size() - 1;
        }

        // 시작점
        int startIndex = Math.max(0, targetIndex - 2);
        // 종료점
        int endIndex = Math.min(allBooks.size(), startIndex + 5);
        // 만약 타겟이 맨 끝부분에 있는 경우
        if (endIndex - startIndex < 5 && allBooks.size() >= 5) {
            startIndex = endIndex - 5;
        }

        return allBooks.subList(startIndex, endIndex);
    }
}
