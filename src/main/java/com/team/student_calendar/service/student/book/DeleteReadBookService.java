package com.team.student_calendar.service.student.book;

import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteReadBookService {

    private final StudentBookRepository studentBookRepository;
    private final SelectStudentService selectStudentService;



    @Transactional
    public void deleteReadBooks(Long studentId) {

        /* 학생 찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 학생이 읽은 책 모두 지우기*/
        studentBookRepository.deleteAllByStudent(student);
    }

}
