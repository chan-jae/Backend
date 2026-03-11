package com.team.student_calendar.service.student;

import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectStudentService {

    private final StudentRepository studentRepository;


    /**
     * 같은 accountNo를 가지는 StudentEntity 가져오기
     * */
    @Transactional(readOnly = true)
    public List<StudentEntity> findAllByAccountNoList(List<Long> bookNoList) {

        return studentRepository.findAllByAccountNoIn(bookNoList);
    }


    @Transactional(readOnly = true)
    public long countAll() {

        return studentRepository.count();
    }
}
