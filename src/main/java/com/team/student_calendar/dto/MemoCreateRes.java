package com.team.student_calendar.dto;

import com.team.student_calendar.entity.MemoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MemoCreateRes {
    private Long memoId; // DB에 저장된 고유 번호
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    // Entity 받은거 바로 DTO로 변환
    public MemoCreateRes(MemoEntity memo) {
        this.memoId = memo.getId();
        this.title = memo.getTitle();
        this.startDate = memo.getStartDate();
        this.endDate = memo.getEndDate();
    }
}