package com.team.student_calendar.service.apiusage;

import com.team.student_calendar.dto.ApiUsageRes;
import com.team.student_calendar.repository.ApiUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectApiUsageService {

    private final ApiUsageRepository apiUsageRepository;


    /**
     * 외부 API별 오늘 사용량과 남은 횟수 조회 (날짜는 서버 Asia/Seoul 기준)
     * @return api_usage에 있는 API 전체
     */
    public List<ApiUsageRes> findApiUsages() {

        LocalDate today = LocalDate.now();
        return apiUsageRepository.findAll().stream()
                .map(entity -> ApiUsageRes.from(entity, today))
                .toList();
    }
}
