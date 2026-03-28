package com.team.student_calendar.service.student;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.FirstLevelReq;
import com.team.student_calendar.entity.StudentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.team.student_calendar.common.constant.RegexPattern.*;

@RequiredArgsConstructor
@Service
public class UpdateStudentService {

    private final SelectStudentService selectStudentService;


    @Transactional
    public void updateStudentFirstLevel(Long studentId, FirstLevelReq req) {

        /* 학생 찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 레벨이 유효한지 체크*/
        String firstLevel = req.getFirstLevel();
        if (!LEVEL.matches(firstLevel)) {
            throw new BaseException(StudentErrorCode.INVALID_FIRST_LEVEL);
        }

        /* 레벨 변경*/
        student.setFirstLevel(req.getFirstLevel());
    }
}
