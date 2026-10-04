package com.team.student_calendar.security.service;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.JWTException;
import com.team.student_calendar.security.entity.RefreshTokenEntity;
import com.team.student_calendar.security.entity.UserEntity;
import com.team.student_calendar.security.repository.RefreshTokenRepository;
import com.team.student_calendar.security.repository.UserRepository;
import com.team.student_calendar.security.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int MAX_REFRESH_TOKENS_PER_USER = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;


    @Transactional
    public void insertRefreshToken(String username, String refreshToken, String clientIp) {

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setRefresh(refreshToken);
        entity.setRegisteredIp(clientIp);
        entity.setUpdatedIp(clientIp);

        refreshTokenRepository.save(entity);
    }


    @Transactional
    public String[] reissueTokens(String refreshToken, String clientIp) {

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
        // 서명은 유효한데 DB에 없음 = 로테이션으로 폐기됐거나 로그아웃된 토큰 재사용 (탈취 의심)
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findFirstByRefresh(refreshToken)
                .orElseThrow(() -> {
                    log.warn("revoked refresh token reused [{}]", claims.getSubject());
                    return new BaseException(JWTException.INVALID_REFRESH_TOKEN);
                });

        // 권한은 토큰이 아닌 DB에서 읽음 (권한 변경/탈퇴가 재발급 시점에 바로 반영되도록)
        String username = claims.getSubject();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(JWTException.INVALID_REFRESH_TOKEN));
        String role = "ROLE_" + user.getRole().name();

        // 재발급 (Refresh 토큰 로테이션: 기존 토큰 폐기 후 새로 발급)

        String newAccessToken = jwtUtil.createAccessToken(username, role);
        String newRefreshToken = jwtUtil.createRefreshToken(username, role);

        // 리프레시 토큰 갱신
        refreshTokenEntity.setRefresh(newRefreshToken);
        refreshTokenEntity.setUpdatedIp(clientIp);

        return new String[] { newAccessToken, newRefreshToken };
    }


    @Transactional
    public void cleanupRefreshTokens(String username) {

        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMillis(jwtUtil.getRefreshTokenExpireTime()));

        List<String> usernames;
        if (username == null) {
            usernames = refreshTokenRepository.findDistinctUsernames();
        }
        else {
            usernames = Collections.singletonList(username);
        }

        for (String name : usernames) {

            // refreshTokenExpireTime 이 지난 토큰 삭제
            refreshTokenRepository.deleteByUsernameAndRegisteredAtBefore(name, cutoff);

            // 남은 토큰이 5개를 초과하면 오래된 순으로 초과분 삭제
            List<RefreshTokenEntity> remaining = refreshTokenRepository.findByUsernameOrderByRegisteredAtDesc(name);

            if (remaining.size() > MAX_REFRESH_TOKENS_PER_USER) {
                List<RefreshTokenEntity> excess = remaining.subList(MAX_REFRESH_TOKENS_PER_USER, remaining.size());
                refreshTokenRepository.deleteAll(excess);
            }
        }
    }


    @Transactional
    public void deleteRefreshToken(String refreshToken) {

        refreshTokenRepository.deleteByRefresh(refreshToken);
    }
}
