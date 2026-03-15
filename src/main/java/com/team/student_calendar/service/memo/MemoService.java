package com.team.student_calendar.service.memo;

import com.team.student_calendar.dto.MemoCreateReq;
import com.team.student_calendar.dto.MemoCreateRes;
import com.team.student_calendar.entity.MemoEntity;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.repository.MemoRepository;
import com.team.student_calendar.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public MemoCreateRes createMemo(Long studentId, MemoCreateReq req) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다." + studentId));

        MemoEntity memo = MemoEntity.builder()
                .studentEntity(student) // 학생 정보 통으로
                .title(req.getTitle())
                .content(req.getContent())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();

        MemoEntity savedMemo = memoRepository.save(memo);

        return new MemoCreateRes(savedMemo);
    }
}
