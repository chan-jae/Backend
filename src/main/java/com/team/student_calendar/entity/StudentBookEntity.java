package com.team.student_calendar.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.student_calendar.dto.ReadBooksRes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

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


    public ReadBooksRes toReadBooksRes() {
        ReadBooksRes res = new ReadBooksRes();
        res.setId(book.getId());
        res.setTitle(book.getTitle());
        res.setAuthor(book.getAuthor());
        res.setPublisher(book.getPublisher());
        res.setCategory(book.getCategory());
        res.setLevel(book.getLevel());
        res.setDifficulty(book.getDifficulty());
        res.setBookNo(book.getBookNo());
        res.setImageUrl(book.getImageUrl());
        res.setType(book.getType());
        res.setState(state);

        return res;
    }
}
