package com.team.student_calendar.service.student;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.repository.StudentRepository;
import com.team.student_calendar.repository.jdbc.StudentJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteStudentService {

    private final SelectStudentService selectStudentService;
    private final StudentRepository studentRepository;
    private final StudentJdbcRepository studentJdbcRepository;
    private final StudentBookRepository studentBookRepository;



    @Transactional
    public void deleteStudent(Long id) {

        /* 학생 있는지 체크*/
        boolean isExist = selectStudentService.existsById(id);
        if (!isExist) {
            throw new BaseException(StudentErrorCode.STUDENT_NOT_FOUND);
        }

        /* 학생이 읽은 책 삭제*/
        studentBookRepository.deleteAllByStudent_Id(id);

        /* 학생 삭제*/
        studentRepository.deleteById(id);
    }
}
