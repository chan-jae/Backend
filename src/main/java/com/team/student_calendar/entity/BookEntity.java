package com.team.student_calendar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.team.student_calendar.common.enums.BookType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@DynamicInsert
@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "book", schema = "student_calendar")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "book")
    private List<StudentBookEntity> studentBooks;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "author", nullable = false, length = 30)
    private String author;

    @Column(name = "publisher", nullable = false, length = 20)
    private String publisher;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "level", nullable = false, length = 10)
    private String level;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "book_no", nullable = false, unique = true)
    private Long bookNo;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "type", nullable = false)
    @ColumnDefault("0")
    private Byte type;

}