package com.team.student_calendar.service.book;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookRepository bookRepository;
    private final SelectBookService selectBookService;



//    @Transactional
//    public void saveBook() {
//
//    }


    @Transactional
    public void saveBookList(List<BookCreateReq> bookList) {

        // bookNo만 리스트로 뽑기
        List<Integer> bookNoList = bookList.stream()
                .map(BookCreateReq::getBookNo)
                .toList();

        // 이미 존재하는 엔티티 가져오기
        List<BookEntity> existingBookEntity = selectBookService
                .findAllByBookNoList(bookNoList);

        // 이미 존재하는 bookNo만 뽑기
        Set<Integer> existingNoSet = existingBookEntity.stream()
                .map(BookEntity::getBookNo)
                .collect(Collectors.toSet());

        // 이미 존재하는 책을 빼고 엔티티로 변환
        List<BookEntity> bookEntityList = bookList.stream()
                // 이미 존재하는거 제외 (새로운 책만 저장)
                .filter(b -> !existingNoSet.contains(b.getBookNo()))
                .map(BookCreateReq::toEntity)
                .toList();

        // 저장
        bookRepository.saveAll(bookEntityList);
    }
}
