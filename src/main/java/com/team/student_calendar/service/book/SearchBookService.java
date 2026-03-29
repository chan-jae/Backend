package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchBookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<BookSliderRes> searchBooks(String keyword) {

        String cleanKeyword = keyword.replaceAll("\\s+", "");

        List<BookEntity> searchedBooks = bookRepository.searchBooksByKeyword(cleanKeyword);

        return searchedBooks.stream()
                .map(book -> new BookSliderRes(
                        book.getBookNo(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCategory(),
                        book.getImageUrl(),
                        false // 검색 결과이므로 기본적으로 '읽음' 상태는 false로 둡니다.
                ))
                .collect(Collectors.toList());
    }
}