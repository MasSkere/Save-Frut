package com.savefrut.msordenes.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Guarda en Redis la respuesta asociada a una Idempotency-Key para que un reintento
 * (doble clic, reenvío de red) devuelva el mismo resultado sin volver a cobrar.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    public Optional<String> obtenerRespuesta(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + key));
    }

    public void guardarRespuesta(String key, String respuesta) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + key, respuesta, TTL);
    }
}
