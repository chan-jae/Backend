package com.team.student_calendar.entity;

import com.team.student_calendar.common.enums.IsbnLookupError;
import com.team.student_calendar.common.enums.IsbnLookupStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "isbn_lookup", schema = "student_calendar")
public class IsbnLookupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "isbn", nullable = false, length = 13)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private IsbnLookupStatus status;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "class_no", length = 20)
    private String classNo;

    @Column(name = "category", length = 20)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "error", length = 10)
    private IsbnLookupError error;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
