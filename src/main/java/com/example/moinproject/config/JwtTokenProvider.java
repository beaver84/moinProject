package com.example.moinproject.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userId) {
        try {
            // 토큰 생성 로직
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);

            return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (NumberFormatException e) {
            log.error("JWT 만료 시간 파싱 중 오류 발생: ", e);
            throw new RuntimeException("JWT 만료 시간 설정이 올바르지 않습니다.", e);
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: ", e);
            throw new RuntimeException("JWT 토큰을 생성할 수 없습니다.", e);
        }
    }
}