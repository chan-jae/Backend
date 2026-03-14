package com.team.student_calendar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.student_calendar.dto.StudentCreateReq;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.LocalDate;

@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "student", schema = "student_calendar")

public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "student")
    private BookProgressEntity bookProgress;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "login_id", nullable = false, length = 20)
    private String loginId;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "grade", nullable = false, length = 20)
    private String grade;

    @Column(name = "level", nullable = false, length = 10)
    private String level;

    @Column(name = "account_no", nullable = false)
    private Long accountNo;


    public void applyChanges(StudentCreateReq req) {
        this.name = req.getName();
        this.loginId = req.getLoginId();
        this.phone = req.getPhone();
        this.grade = req.getGrade();
        this.level = req.getLevel();
        this.accountNo = req.getAccountNo();
    }
}
