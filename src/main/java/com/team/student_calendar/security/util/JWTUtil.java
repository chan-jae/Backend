package com.team.student_calendar.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey key;
    private final long accessTokenExpireTime;
    @Getter
    private final long refreshTokenExpireTime;


    public JWTUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire-time}") long accessTokenExpireTime,
            @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime
    ) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }


    /**
     * ACCESS 토큰 발급
     * @param username 유저 ID
     * @param role 유저 권한
     * @return ACCESS 토큰
     */
    public String createAccessToken(String username, String role) {

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessTokenExpireTime);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("tokenType", "ACCESS")
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }


    /**
     * REFRESH 토큰 발급
     * @param username 유저 ID
     * @param role 유저 권한
     * @return ACCESS 토큰
     */
    public String createRefreshToken(String username, String role) {

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshTokenExpireTime);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("tokenType", "REFRESH")
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }


    /**
     * JWT 검증 및 읽기
     * @param token
     * @return
     */
    public Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // 프론트/백엔드가 같은 도메인(same-site)이라 SameSite=Strict 사용.
    public ResponseCookie createRefreshTokenCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/r-token")
                .maxAge(Duration.ofMillis(refreshTokenExpireTime))
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/r-token")
                .maxAge(Duration.ZERO) // 0 이어야 브라우저가 쿠키를 삭제함
                .build();
    }
}
