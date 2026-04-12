package com.team.student_calendar.entity;


import com.team.student_calendar.dto.ReadBooksRes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@DynamicInsert
@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "student_book", schema = "student_calendar")
public class StudentBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private BookEntity book;

    @Column(name = "state", nullable = false)
    @ColumnDefault("0")
    private Byte state;

    @Column(name = "read_at")
    private LocalDate readAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;


    public ReadBooksRes.Book toReadBooksRes() {

        ReadBooksRes.Book resBook = new ReadBooksRes.Book();
        resBook.setId(book.getId());
        resBook.setTitle(book.getTitle());
        resBook.setAuthor(book.getAuthor());
        resBook.setPublisher(book.getPublisher());
        resBook.setCategory(book.getCategory());
        resBook.setLevel(book.getLevel());
        resBook.setDifficulty(book.getDifficulty());
        resBook.setBookNo(book.getBookNo());
        resBook.setImageUrl(book.getImageUrl());
        resBook.setType(book.getType());
        resBook.setState(state);
        resBook.setReadAt(this.readAt);

        return resBook;
    }
}
