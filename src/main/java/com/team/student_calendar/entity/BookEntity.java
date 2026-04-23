package com.team.student_calendar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.student_calendar.dto.RecBookRes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@DynamicInsert
@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "book",
        schema = "student_calendar",
        indexes = {
        @Index(
                name = "idx_book_updated_title",
                columnList = "difficulty ASC, title ASC"
        )
})
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

    @Column(name = "c_level", nullable = false)
    private Byte cLevel;

    @Column(name = "book_no", nullable = false, unique = true)
    private Long bookNo;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "type", nullable = false)
    @ColumnDefault("0")
    private Byte type;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;




    public RecBookRes toRecBook(Byte state, LocalDate readAt) {

        RecBookRes dto = new RecBookRes();

        dto.setId(this.id);
        dto.setTitle(this.title);
        dto.setAuthor(this.author);
        dto.setPublisher(this.publisher);
        dto.setCategory(this.category);
        dto.setLevel(this.level);
        dto.setDifficulty(this.difficulty);
        dto.setCLevel(this.cLevel);
        dto.setBookNo(this.bookNo);
        dto.setImageUrl(this.imageUrl);
        dto.setType(this.type);
        dto.setUpdatedAt(this.updatedAt);

        dto.setState(state);
        dto.setReadAt(readAt);

        return dto;
    }
}