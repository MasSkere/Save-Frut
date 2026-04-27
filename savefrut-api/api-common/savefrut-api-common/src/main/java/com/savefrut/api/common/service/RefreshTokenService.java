package com.savefrut.api.common.service;

import com.savefrut.api.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Servicio para manejar refresh tokens de forma segura en Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    /**
     * Almacena un refresh token en Redis con TTL.
     * @param userId ID del usuario
     * @param refreshToken El token a almacenar
     */
    public void storeRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        // TTL de 7 días + 1 hora extra para evitar race conditions
        Duration ttl = Duration.ofDays(8);
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
        log.debug("Refresh token almacenado para userId: {}", userId);
    }

    /**
     * Obtiene el refresh token de Redis para un usuario.
     * @param userId ID del usuario
     * @return El refresh token o null si no existe
     */
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * Elimina el refresh token de Redis (logout).
     * @param userId ID del usuario
     */
    public void revokeRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh token revocado para userId: {}", userId);
    }

    /**
     * Valida y rota un refresh token.
     * @param refreshToken El token a validar
     * @return Nuevo refresh token si es válido, null si no
     */
    public String validateAndRotateRefreshToken(String refreshToken) {
        try {
            // Validar el token JWT
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.warn("Refresh token inválido");
                return null;
            }

            // Extraer userId
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

            // Verificar que existe en Redis
            String storedToken = getRefreshToken(userId);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                log.warn("Refresh token no encontrado en Redis o no coincide para userId: {}", userId);
                return null;
            }

            // Generar nuevo refresh token
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

            // Almacenar el nuevo
            storeRefreshToken(userId, newRefreshToken);

            log.debug("Refresh token rotado exitosamente para userId: {}", userId);
            return newRefreshToken;

        } catch (Exception e) {
            log.error("Error al validar/rotar refresh token: {}", e.getMessage());
            return null;
        }
    }
}
