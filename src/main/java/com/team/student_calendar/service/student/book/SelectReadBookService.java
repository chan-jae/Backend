package com.team.student_calendar.service.student.book;

import com.team.student_calendar.dto.ReadBooksRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentBookRepository;
import com.team.student_calendar.service.student.SelectStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SelectReadBookService {

    private final StudentBookRepository studentBookRepository;
    private final SelectStudentService selectStudentService;



    /**
     * 읽지 않은 책이 맞는지
     */
    @Transactional(readOnly = true)
    public boolean isReadBook(
            Long studentId,
            Long bookId
    ) {

        Optional<StudentBookEntity> optional = studentBookRepository.findByStudentIdAndBookId(studentId, bookId);
        return optional.isPresent();
    }


    /**
     * 학생이 읽은 책 가져오기
     * @param studentId 학생 pk
     * @return List<ReadBooksRes>
     */
    @Transactional(readOnly = true)
    public List<ReadBooksRes> findReadBooksByStudentId(Long studentId) {

        /* 학생 찾기*/
        StudentEntity student = selectStudentService.findById(studentId);

        /* 읽은 책 목록 모두 가져오기*/
        List<StudentBookEntity> readBookList = studentBookRepository.findByStudentId(studentId);

        /* ReadBooksRes로 바꾸기*/
        List<ReadBooksRes> readBooksResList = readBookList.stream()
                .map(StudentBookEntity::toReadBooksRes)
                .toList();

        return readBooksResList;
    }
}
