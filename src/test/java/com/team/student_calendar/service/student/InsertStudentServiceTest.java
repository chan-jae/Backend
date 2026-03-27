package com.team.student_calendar.service.student;

import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsertStudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SelectStudentService selectStudentService;

    @InjectMocks
    private InsertStudentService insertStudentService;



    @Test
    @DisplayName("빈 목록이면 false 반환")
    void saveStudentList_whenEmpty_returnsFalse() {

        // given
        when(selectStudentService.findAllByAccountNoList(List.of())).thenReturn(List.of());

        // when
        boolean result = insertStudentService.saveStudentList(List.of());

        // then
        /* 삽입된 데이터가 없으므로 false 반환해야함*/
        assertThat(result).isFalse();
        /* save() 호출이 0번이어야 함*/
        verify(studentRepository, never()).save(any());
    }


    @Test
    @DisplayName("모두 기존 학생이면 save 호출 안되고 false 반환")
    void saveStudentList_whenAllExisting_doesNotSaveAndReturnsFalse() {

        // given
        int testDataCount = 2;
        List<StudentCreateReq> reqs = new ArrayList<>();
        List<StudentEntity> existingList = new ArrayList<>();
        List<Long> accountNos = new ArrayList<>();
        for (long i = 1; i <= testDataCount; i++) {
            accountNos.add(i);
            reqs.add(sampleReq(i, "Updated" + i));
            existingList.add(StudentEntity.builder()
                    .id(100L + i)
                    .accountNo(i)
                    .name("Old" + i)
                    .loginId("oldId" + i)
                    .phone("010-" + i)
                    .grade("1")
                    .level("A")
                    .joinedAt(LocalDateTime.of(2024, (int) i, 1, 0, 0))
                    .build());
        }
        when(selectStudentService.findAllByAccountNoList(accountNos)).thenReturn(existingList);

        // when
        boolean result = insertStudentService.saveStudentList(reqs);

        // then
        /* 새로운 데이터가 없으니까 false 반환*/
        assertThat(result).isFalse();
        /* 변경사항이 적용되었는지 체크*/
        for (int i = 0; i < testDataCount; i++) {
            assertThat(existingList.get(i).getName()).isEqualTo("Updated" + (i + 1));
        }
        /* save() 메서드 호출 안되었는지 체크*/
        verify(studentRepository, never()).save(any());
    }


    @Test
    @DisplayName("신규 학생만 있으면 저장마다 save 호출되고 true 반환")
    void saveStudentList_whenAllNew_savesEachAndReturnsTrue() {

        // given
        int newCount = 2;
        List<String> names = List.of("Alice", "Bob");
        List<StudentCreateReq> reqs = new ArrayList<>();
        List<Long> accountNos = new ArrayList<>();
        for (int idx = 0; idx < newCount; idx++) {
            long accountNo = idx + 1L;
            accountNos.add(accountNo);
            reqs.add(sampleReq(accountNo, names.get(idx)));
        }
        when(selectStudentService.findAllByAccountNoList(accountNos)).thenReturn(List.of());

        // when
        boolean result = insertStudentService.saveStudentList(reqs);

        // then
        assertThat(result).isTrue();
        verify(studentRepository, times(newCount)).save(any(StudentEntity.class));
    }


    @Test
    @DisplayName("신규와 기존이 섞이면 신규만 save 및 true 반환")
    void saveStudentList_whenMixed_savesOnlyNewAndReturnsTrue() {

        // given
        StudentCreateReq existingReq = sampleReq(1L, "ExistingUpdated");
        StudentCreateReq newReq = sampleReq(2L, "NewStudent");
        StudentEntity existingEntity = StudentEntity.builder()
                .id(10L)
                .accountNo(1L)
                .name("Prev")
                .loginId("e1")
                .phone("111")
                .grade("2")
                .level("B")
                .joinedAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .build();
        when(selectStudentService.findAllByAccountNoList(List.of(1L, 2L)))
                .thenReturn(List.of(existingEntity));

        // when
        boolean result = insertStudentService.saveStudentList(List.of(existingReq, newReq));

        // then
        assertThat(result).isTrue();
        assertThat(existingEntity.getName()).isEqualTo("ExistingUpdated");
        ArgumentCaptor<StudentEntity> captor = ArgumentCaptor.forClass(StudentEntity.class);
        verify(studentRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getAccountNo()).isEqualTo(2L);
        assertThat(captor.getValue().getName()).isEqualTo("NewStudent");
    }





    private static StudentCreateReq sampleReq(long accountNo, String name) {
        StudentCreateReq req = new StudentCreateReq();
        req.setName(name);
        req.setLoginId("login" + accountNo);
        req.setPhone("010-" + accountNo);
        req.setGrade("1");
        req.setLevel("L1");
        req.setAccountNo(accountNo);
        req.setStateStr("ACTIVE");
        req.setJoinedAt(LocalDateTime.of(2025, 1, 1, 9, 0));
        return req;
    }
}
