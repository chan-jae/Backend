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

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

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
    public String reissueAccessToken(String refreshToken) {

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
        boolean exists = refreshTokenRepository.existsByRefresh(refreshToken);

        if (!exists) {
            throw new BaseException(JWTException.INVALID_REFRESH_TOKEN);
        }

        // 재발급
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        return jwtUtil.createAccessToken(username, role);
    }
}
