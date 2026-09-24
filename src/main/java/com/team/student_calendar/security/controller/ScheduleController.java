package com.team.student_calendar.security.controller;

import com.team.student_calendar.security.service.RefreshTokenService;
import com.team.student_calendar.security.service.RegisterTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final RefreshTokenService refreshTokenService;
    private final RegisterTokenService registerTokenService;

    // 매일 새벽 4시, 만료된 리프레시 토큰 삭제 후 유저당 최근 5개만 남기고 정리
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupRefreshTokens() {

        log.info("[scheduler] start cleanup refresh tokens");
        refreshTokenService.cleanupRefreshTokens(null);
        log.info("[scheduler] finish cleanup refresh tokens");
    }

    // 매일 새벽 4시, 발급 후 30분이 지난 가입 토큰 정리
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupRegisterTokens() {

        log.info("[scheduler] start cleanup register tokens");
        registerTokenService.cleanupExpiredTokens();
        log.info("[scheduler] finish cleanup register tokens");
    }
}
