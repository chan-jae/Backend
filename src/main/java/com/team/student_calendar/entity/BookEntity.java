package com.team.student_calendar.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private UserEntity userId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "author", nullable = false, length = 30)
    private String author;

    @Column(name = "publisher", nullable = false, length = 20)
    private String publisher;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "level", length = 3)
    private String level;

    @Column(name = "difficulty")
    private Integer difficulty;

    @Column(name = "book_no")
    private Integer bookNo;

    @Column(name = "image_url")
    private String imageUrl;

}