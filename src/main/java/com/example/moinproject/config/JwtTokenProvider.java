package com.example.moinproject.config;

import com.example.moinproject.config.exception.CustomAuthenticationException;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Component
public class JwtTokenProvider {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public JwtTokenProvider(ObjectMapper objectMapper, UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    @Value("${jwt.secret}")
    private String secretKey;

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUserId())
                .setHeader(createHeader())
                .setClaims(createUserClaims(user))
                .signWith(SignatureAlgorithm.HS256, createSigningKey());

        return builder.compact();
    }

    public User getUserFromJwt(String jwt) {
        try {
            // JWT 토큰에서 Claims 추출
            String tokenWithoutBearer = jwt.replace("Bearer ", "");

            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                    .parseClaimsJws(tokenWithoutBearer)
                    .getBody();

            // Claims에서 userId 추출
            String userId = claims.get("id").toString();

            // userId로 사용자 조회
            return userRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomAuthenticationException("User not found"));
        } catch (Exception e) {
            throw new CustomAuthenticationException("Invalid JWT token");
        }
    }

    private Key createSigningKey() {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    private Map<String, Object> createUserClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        try {
            claims.put("claims", objectMapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting user to JSON", e);
        }
        return claims;
    }
}