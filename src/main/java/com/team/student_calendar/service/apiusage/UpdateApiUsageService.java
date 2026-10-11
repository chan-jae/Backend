package com.team.student_calendar.service.apiusage;

import com.team.student_calendar.common.enums.ApiUsageType;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.repository.ApiUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateApiUsageService {

    private final ApiUsageRepository apiUsageRepository;


    /**
     * 외부 API를 보내기 직전에 호출 횟수 +1 (요청 성공/실패와 상관없이 보내기만 하면 셈)
     * 하나라도 오늘 한도를 넘었으면 전부 롤백하고 에러 -> 보내지 않은 API는 세지 않음
     * 날짜는 서버(JVM, Asia/Seoul) 기준, 마지막 호출 날짜가 오늘이 아니면 1부터 다시 셈
     * @param types 같이 보낼 API들
     */
    @Transactional
    public void increaseCallCount(ApiUsageType... types) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        // api_usage에 API별 행이 미리 있어야 함 (행이 없어도 0이 나와 초과로 처리됨)
        for (ApiUsageType type : types) {
            if (apiUsageRepository.increaseCallCount(type, type.getDailyLimit(), todayStart, now) == 0) {
                log.warn("api limit exceeded - api: {}, dailyLimit: {}", type, type.getDailyLimit());
                throw new BaseException(CommonErrorCode.API_LIMIT_EXCEEDED);
            }
        }
    }
}
