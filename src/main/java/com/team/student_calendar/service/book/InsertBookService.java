package com.team.student_calendar.service.book;

import com.team.student_calendar.common.dto.BookCreateReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsertBookService {

    private final BookRepository bookRepository;



//    @Transactional
//    public void saveBook() {
//
//    }


    @Transactional
    public void saveBookList(List<BookCreateReq> bookList) {

        // 엔티티로 변환
        List<BookEntity> bookEntityList = bookList.stream()
                .map(BookCreateReq::toEntity)
                .toList();

        // 저장
        bookRepository.saveAll(bookEntityList);
    }
}
