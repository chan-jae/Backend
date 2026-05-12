package com.team.student_calendar.service.book;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.dto.RecBookMapping;
import com.team.student_calendar.dto.RecBookRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectBookService {

    private final BookRepository bookRepository;

//    /**
//     * List안에 있는 bookNo를 가지는 BookEntity 가져오기
//     */
//    @Transactional(readOnly = true)
//    public List<BookEntity> findAllByBookNoList(List<Long> bookNoList) {
//        return bookRepository.findAllByBookNoIn(bookNoList);
//    }

//    /**
//     * 책 목록 페이징
//     */
//    @Transactional(readOnly = true)
//    public Page<BookEntity> getBookListWithPaging(int page, int size) {
//        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "bookNo"));
//        return bookRepository.findAll(pageable);
//    }


    /**
     * 난이도, 제목순으로 가져오기
     * @return 책 list
     */
    @Transactional(readOnly = true)
    public List<BookEntity> findAllByDifficultyAscAndTitleAsc() {
        Sort sort = Sort.by(
                Sort.Order.asc("difficulty"),
                Sort.Order.asc("title"));
        return bookRepository.findAll(sort);
    }


    @Transactional(readOnly = true)
    public BookEntity findById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() -> new BaseException(BookErrorCode.BOOK_NOT_FOUND));
    }


//    /**
//     * 전체 책 개수 가져오기
//     */
//    @Transactional(readOnly = true)
//    public long countAll() {
//        return bookRepository.count();
//    }


    /**
     * 현재 수업에서 읽을 문학 책 가져오기
     * @param studentId 학생 pk
     * @param baseLevel 기준 레벨
     * @return 책 array
     */
    @Transactional(readOnly = true)
    public RecBookRes[] findLiteratureBookToRead(
            Long studentId,
            Byte baseLevel) {

        List<RecBookMapping> bookMapping = bookRepository
                .findFirstUnreadBookAboveCLevelForLiterature(studentId, baseLevel, recBookQueryOption());

        return mappingToDto(bookMapping);
    }

    /**
     * 현재 수업에서 읽을 비문학 책 가져오기
     * @param studentId 학생 pk
     * @param baseLevel 기준 레벨
     * @return 책 array
     */
    @Transactional(readOnly = true)
    public RecBookRes[] findNonLiteratureBookToRead(
            Long studentId,
            Byte baseLevel) {

        List<RecBookMapping> bookMapping = bookRepository
                .findFirstUnreadBookAboveCLevelForNonLiterature(studentId, baseLevel, recBookQueryOption());

        return mappingToDto(bookMapping);
    }







    private RecBookRes[] mappingToDto(List<RecBookMapping> bookMapping) {

        return bookMapping.stream().map(m -> {
            RecBookRes res = new RecBookRes();
            BookEntity book = m.getBook();

            try {
                BeanUtils.copyProperties(book, res);
                res.setState(m.getState());
                res.setReadAt(m.getReadAt());
            } catch (Exception e) {
                throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            return res;
        }).toArray(RecBookRes[]::new);
    }


    /**
     * 난이도 낮은 순으로 읽어야 하는 책 2권 가져오기
     *
     * <p>- 읽던 책이 있다면 해당 책 우선 읽어야 함</p>
     * @return pagable
     */
    private Pageable recBookQueryOption() {

        Sort sort = Sort.by(
                Sort.Order.asc("sb.state").nullsLast(),
                Sort.Order.asc("b.difficulty"),
                Sort.Order.asc("b.title")
        );

        return PageRequest.of(0, 2, sort);
    }
}