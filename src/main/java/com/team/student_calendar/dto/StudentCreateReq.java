package com.team.student_calendar.dto;

import com.team.student_calendar.entity.StudentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCreateReq {

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;

    @NotBlank(message = "로그인 아이디는 필수 항목입니다.")
    private String loginId;

    @NotBlank(message = "전화번호는 필수 항목입니다.")
    private String phone;

    @NotBlank(message = "학년은 필수 항목입니다.")
    private String grade;

    @NotBlank(message = "레벨은 필수 항목입니다.")
    private String level;

    @NotNull(message = "계정 넘버는 필수 항목입니다.")
    private Long accountNo;


    public StudentEntity toEntity() {
        return StudentEntity.builder()
                .name(this.name)
                .loginId(this.loginId)
                .phone(this.phone)
                .grade(this.grade)
                .level(this.level)
                .accountNo(this.accountNo)
                .build();
    }
}
