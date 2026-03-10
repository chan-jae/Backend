package com.team.student_calendar.service.student;

import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class InsertStudentService {

    private final StudentRepository studentRepository;
    private final SelectStudentService selectStudentService;



    /**
     * List 타입 학생들을 모두 저장
     * @param  studentCreateReqList List 타입 학생들
     * @return 1개라도 삽입된 데이터가 있는지?
     * */
    @Transactional
    public boolean saveStudentList(List<StudentCreateReq> studentCreateReqList) {

        log.info("[InsertStudentService.saveStudentList] 학생 등록 시작 - 요청 건수: {}건", studentCreateReqList.size());

        // 몇번 추가되었는지 체크
        int insertCount = 0;

        // accountNo만 리스트로 뽑기
        List<Long> accountNoList = studentCreateReqList.stream()
                .map(StudentCreateReq::getAccountNo)
                .toList();

        // 이미 존재하는 student 가져오기
        Map<Long, StudentEntity> existingMap = selectStudentService
                .findAllByAccountNoList(accountNoList)
                .stream()
                .collect(Collectors.toMap(StudentEntity::getAccountNo, s -> s));

        /*
        * accountNo를 가지는 튜플이 있음 -> applyChanges()
        * accountNo를 가지는 튜플이 없음 -> 새로 삽입
        * */
        for (StudentCreateReq dto : studentCreateReqList) {
            StudentEntity studentEntity = existingMap.get(dto.getAccountNo());

            // 이미 있는값이면 필드 수정
            if (studentEntity != null) {
                // 변경만 해놓으면 @DynamicInsert가 알아서 변경된 값으로 Isnert 해줌
                studentEntity.applyChanges(dto);
                continue;
            }
            // 없으면 삽입
            StudentEntity insertingEntity = dto.toEntity();
            studentRepository.save(insertingEntity);
            insertCount++;
        }

        log.info("[InsertStudentService.saveStudentList] 학생 등록 끝 - 삽입 건수: {}건", insertCount);

        return (insertCount > 0);
    }
}
