package com.team.student_calendar.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.student_calendar.dto.ReadBooksRes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private BookEntity book;

    @Column(name = "state", nullable = false)
    @ColumnDefault("0")
    private Byte state;

    @Column(name = "read_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime readAt;

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
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

        return resBook;
    }
}
