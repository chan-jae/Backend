package com.team.student_calendar.service.student;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
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


//    /**
//     * 같은 accountNo를 가지는 StudentEntity 가져오기
//     * */
//    @Transactional(readOnly = true)
//    public List<StudentEntity> findAllByAccountNoList(List<Long> bookNoList) {
//
//        return studentRepository.findAllByAccountNoIn(bookNoList);
//    }
//
//
//    @Transactional(readOnly = true)
//    public long countAll() {
//
//        return studentRepository.count();
//    }


    /**
     * 최근 가입 순으로 학생 가져오기
     * @return 학생 entity list
     */
    @Transactional(readOnly = true)
    public List<StudentEntity> findAllLatestStudents() {

        return studentRepository.findAllByOrderByJoinedAtDesc();
    }


    @Transactional(readOnly = true)
    public StudentEntity findById(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(() -> new BaseException(StudentErrorCode.STUDENT_NOT_FOUND));
    }


    public boolean existsById(Long id) {

        return studentRepository.existsById(id);
    }
}
