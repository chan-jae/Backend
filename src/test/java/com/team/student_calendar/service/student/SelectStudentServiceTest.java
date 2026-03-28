package com.team.student_calendar.service.student;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SelectStudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private SelectStudentService selectStudentService;



    @Test
    @DisplayName("존재하지 않는 pk로 학생 조회하면 throw STUDENT_NOT_FOUND")
    void findById_whenNotExist_throwStudentNotFound() {

        // given
        long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        BaseException thrown = assertThrows(BaseException.class, () -> selectStudentService.findById(id));

        // then
        assertThat(thrown.getErrorCode()).isEqualTo(StudentErrorCode.STUDENT_NOT_FOUND);
    }
}
