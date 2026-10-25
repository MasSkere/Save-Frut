package com.savefrut.msidentidad.service;

import com.savefrut.msidentidad.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    public void storeRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        Duration ttl = Duration.ofDays(8);
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
        log.debug("Refresh token almacenado para userId: {}", userId);
    }

    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    public void revokeRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh token revocado para userId: {}", userId);
    }

    public String validateAndRotateRefreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.warn("Refresh token inválido");
                return null;
            }

            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

            String storedToken = getRefreshToken(userId);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                log.warn("Refresh token no encontrado en Redis o no coincide para userId: {}", userId);
                return null;
            }

            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
            storeRefreshToken(userId, newRefreshToken);

            log.debug("Refresh token rotado exitosamente para userId: {}", userId);
            return newRefreshToken;

        } catch (Exception e) {
            log.error("Error al validar/rotar refresh token: {}", e.getMessage());
            return null;
        }
    }
}
