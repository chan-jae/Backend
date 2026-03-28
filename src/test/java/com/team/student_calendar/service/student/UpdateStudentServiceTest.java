package com.team.student_calendar.service.student;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.FirstLevelReq;
import com.team.student_calendar.entity.StudentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UpdateStudentServiceTest {

    @Mock
    private SelectStudentService selectStudentService;

    @InjectMocks
    private UpdateStudentService updateStudentService;


    @Test
    @DisplayName("학생 못찾으면 throw STUDENT_NOT_FOUND")
    void updateStudentFirstLevel_whenNotExist_throwStudentNotFound() {

        // given
        long id = 1L;
        FirstLevelReq req = sampleFirstLevelReq("first level");
        when(selectStudentService.findById(id)).thenThrow(new BaseException(StudentErrorCode.STUDENT_NOT_FOUND));

        // when
        BaseException thrown = assertThrows(BaseException.class, () ->
                updateStudentService.updateStudentFirstLevel(id, req));

        // then
        assertThat(thrown.getErrorCode()).isEqualTo(StudentErrorCode.STUDENT_NOT_FOUND);
    }


    @Test
    @DisplayName("레벨이 유효하지 않으면 throw INVALID_FIRST_LEVEL")
    void updateStudentFirstLevel_whenInvalidLevel_throwInvalidFirstLevel() {

        // given
        long id = 1L;
        FirstLevelReq req = sampleFirstLevelReq("first level");
        StudentEntity studentEntity = sampleStudentEntity(id, null);
        when(selectStudentService.findById(id)).thenReturn(studentEntity);

        // when
        BaseException thrown = assertThrows(BaseException.class, () ->
                updateStudentService.updateStudentFirstLevel(id, req));

        // then
        assertThat(thrown.getErrorCode()).isEqualTo(StudentErrorCode.INVALID_FIRST_LEVEL);
    }


    @Test
    @DisplayName("레벨 변경되었는지 체크")
    void updateStudentFirstLevel_shouldUpdateLevel_whenChange() {

        // given
        long id = 1L;
        String firstLevel = "A_0";
        String changeLevel = "B_1";
        FirstLevelReq req = sampleFirstLevelReq(changeLevel);
        StudentEntity studentEntity = sampleStudentEntity(id, firstLevel);
        when(selectStudentService.findById(id)).thenReturn(studentEntity);

        // when
        updateStudentService.updateStudentFirstLevel(id, req);

        // then
        assertThat(studentEntity.getFirstLevel()).isEqualTo(changeLevel);
    }







    private FirstLevelReq sampleFirstLevelReq(String level) {

        FirstLevelReq req = new FirstLevelReq();
        req.setFirstLevel(level);
        return req;
    }


    private StudentEntity sampleStudentEntity(Long id, String level) {

        return StudentEntity.builder()
                .id(id)
                .level(level)
                .build();
    }
}
