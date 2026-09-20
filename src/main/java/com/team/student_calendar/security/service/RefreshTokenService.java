package com.team.student_calendar.security.service;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.JWTException;
import com.team.student_calendar.entity.RefreshTokenEntity;
import com.team.student_calendar.repository.RefreshTokenRepository;
import com.team.student_calendar.security.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int MAX_REFRESH_TOKENS_PER_USER = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;


    @Transactional
    public void insertRefreshToken(String username, String refreshToken) {

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setRefresh(refreshToken);

        refreshTokenRepository.save(entity);
    }


    @Transactional
    public String[] reissueTokens(String refreshToken) {

        // Refresh 토큰 검증
        Claims claims;
        try {
            claims = jwtUtil.getClaims(refreshToken);
        } catch (Exception e) {
            throw new BaseException(JWTException.INVALID_REFRESH_TOKEN);
        }

        String tokenType = claims.get("tokenType", String.class);

        if (!"REFRESH".equals(tokenType)) {
            throw new BaseException(JWTException.INVALID_REFRESH_TOKEN);
        }

        // Refresh 토큰 db 확인
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByRefresh(refreshToken)
                .orElseThrow(() -> new BaseException(JWTException.INVALID_REFRESH_TOKEN));

        // 재발급 (Refresh 토큰 로테이션: 기존 토큰 폐기 후 새로 발급)
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        String newAccessToken = jwtUtil.createAccessToken(username, role);
        String newRefreshToken = jwtUtil.createRefreshToken(username, role);

        // 리프레시 토큰 갱신
        refreshTokenEntity.setRefresh(newRefreshToken);

        return new String[] { newAccessToken, newRefreshToken };
    }


    @Transactional
    public void cleanupRefreshTokens() {

        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMillis(jwtUtil.getRefreshTokenExpireTime()));

        List<String> usernames = refreshTokenRepository.findDistinctUsernames();

        for (String username : usernames) {

            // refreshTokenExpireTime 이 지난 토큰 삭제
            refreshTokenRepository.deleteByUsernameAndRegisteredAtBefore(username, cutoff);

            // 남은 토큰이 5개를 초과하면 오래된 순으로 초과분 삭제
            List<RefreshTokenEntity> remaining = refreshTokenRepository.findByUsernameOrderByRegisteredAtDesc(username);

            if (remaining.size() > MAX_REFRESH_TOKENS_PER_USER) {
                List<RefreshTokenEntity> excess = remaining.subList(MAX_REFRESH_TOKENS_PER_USER, remaining.size());
                refreshTokenRepository.deleteAll(excess);
            }
        }
    }
}
