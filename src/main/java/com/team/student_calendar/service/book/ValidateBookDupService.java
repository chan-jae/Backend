package com.team.student_calendar.service.book;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.util.BookHashUtil;
import com.team.student_calendar.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ValidateBookDupService {

    private BookRepository bookRepository;


    /**
     * 책제목, 책저자로 이미 추가된 책이 있는지 체크
     */
    @Transactional(readOnly = true)
    public void checkBookDuplication(String title, String author) {

        String bHash = BookHashUtil.generateBookHashKey(title, author);

        if (bookRepository.existsBybHash(bHash)) {
            throw new BaseException(BookErrorCode.ALREADY_EXIST_BOOK);
        }
    }
}
