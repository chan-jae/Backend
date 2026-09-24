package com.team.student_calendar.security.service;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.RegisterTokenErrorCode;
import com.team.student_calendar.security.entity.RegisterTokenEntity;
import com.team.student_calendar.security.repository.RegisterTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterTokenService {

    private static final long REGISTER_TOKEN_EXPIRE_MINUTES = 30;

    private final RegisterTokenRepository registerTokenRepository;


    // ADMIN이 가입 토큰 발급, 기존에 발급된 토큰이 있다면 지우고 항상 1개만 존재하도록 함
    @Transactional
    public String issueToken() {

        registerTokenRepository.deleteAll();

        RegisterTokenEntity entity = new RegisterTokenEntity();
        entity.setToken(UUID.randomUUID().toString());
        entity.setUsed(false);

        registerTokenRepository.save(entity);

        return entity.getToken();
    }


    // 가입 토큰 검증 후 일회용으로 소비. 동시 요청이 같은 토큰을 사용해도 하나만 성공하도록
    // isUsed=false 조건이 있는 원자적 UPDATE로 소비 처리 (삭제 대신 사용)
    @Transactional
    public void validateAndConsume(String token) {

        if (token == null) {
            throw new BaseException(RegisterTokenErrorCode.INVALID_REGISTER_TOKEN);
        }

        RegisterTokenEntity entity = registerTokenRepository.findByToken(token)
                .orElseThrow(() -> new BaseException(RegisterTokenErrorCode.INVALID_REGISTER_TOKEN));

        LocalDateTime expireAt = entity.getRegisteredAt().plusMinutes(REGISTER_TOKEN_EXPIRE_MINUTES);

        if (LocalDateTime.now().isAfter(expireAt)) {
            registerTokenRepository.delete(entity);
            throw new BaseException(RegisterTokenErrorCode.EXPIRED_REGISTER_TOKEN);
        }

        int updated = registerTokenRepository.consumeToken(token);

        if (updated == 0) {
            throw new BaseException(RegisterTokenErrorCode.INVALID_REGISTER_TOKEN);
        }
    }


    // 발급 후 30분이 지난 가입 토큰 정리
    @Transactional
    public void cleanupExpiredTokens() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(REGISTER_TOKEN_EXPIRE_MINUTES);

        registerTokenRepository.deleteByRegisteredAtBefore(cutoff);
    }
}
