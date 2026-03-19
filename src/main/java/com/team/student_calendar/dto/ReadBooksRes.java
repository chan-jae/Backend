package com.team.student_calendar.dto;

import lombok.Data;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReadBooksRes {

    private Long studentId;
    private List<Book> books;
    private boolean hasNext;
    private int pageNumber;


    public static ReadBooksRes of(Long studentId, Slice<Book> bookSlice) {
        ReadBooksRes res = new ReadBooksRes();
        res.setStudentId(studentId);
        res.setBooks(bookSlice.getContent());
        res.setHasNext(bookSlice.hasNext());
        res.setPageNumber(bookSlice.getNumber());
        return res;
    }


    @Data
    public static class Book {
        private Long id;
        private String title;
        private String author;
        private String publisher;
        private String category;
        private String level;
        private Integer difficulty;
        private Long bookNo;
        private String imageUrl;
        private Byte type;
        private Byte state;
        private LocalDate readAt;
    }
}
