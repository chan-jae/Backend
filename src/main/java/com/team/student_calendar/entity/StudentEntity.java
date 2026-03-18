package com.team.student_calendar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.student_calendar.common.enums.StudentState;
import com.team.student_calendar.dto.StudentCreateReq;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    @OneToMany(mappedBy = "student")
    private List<StudentBookEntity> studentBooks;

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

    @Column(name = "state", nullable = false)
    @ColumnDefault("0")
    private Byte state;

    @Column(name = "first_level", length = 10)
    private String firstLevel;

    @Column(name = "joined_at", nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime joinedAt;



    public void applyChanges(StudentCreateReq req) {
        this.name = req.getName();
        this.loginId = req.getLoginId();
        this.phone = req.getPhone();
        this.grade = req.getGrade();
        this.level = req.getLevel();
        this.accountNo = req.getAccountNo();
        this.state = StudentState.getStateFromString(req.getStateStr());
    }
}
