package com.savefrut.api.common.security.jwt;

import com.savefrut.api.common.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Componente responsable de generar, validar y parsear tokens JWT.
 *
 * <p>Maneja dos tipos de tokens:
 * <ul>
 *   <li><b>Access token</b> — vida corta, contiene email, roles e ID del usuario.</li>
 *   <li><b>Refresh token</b> — vida larga, contiene solo el ID del usuario.</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // ─── Constantes ────────────────────────────────────────────────────────────

    private static final String CLAIM_ROLES   = "roles";
    private static final String CLAIM_USER_ID = "userId";
    private static final long   MILLIS_PER_MINUTE = 60_000L;
    private static final long   MILLIS_PER_DAY    = 86_400_000L;

    // ─── Configuración ─────────────────────────────────────────────────────────

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_EXPIRATION_MINUTES}")
    private int accessExpirationMinutes;

    @Value("${JWT_REFRESH_EXPIRATION_DAYS}")
    private int refreshExpirationDays;

    // ─── Generación de tokens ──────────────────────────────────────────────────

    /**
     * Genera un access token JWT para el usuario autenticado.
     *
     * @param authentication contexto de autenticación de Spring Security
     * @return access token firmado
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_USER_ID, userPrincipal.getId())
                .issuedAt(new Date())
                .expiration(expirationDesdeAhora(accessExpirationMinutes * MILLIS_PER_MINUTE))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Genera un refresh token JWT que contiene solo el ID del usuario.
     *
     * @param userId ID del usuario autenticado
     * @return refresh token firmado
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(expirationDesdeAhora(refreshExpirationDays * MILLIS_PER_DAY))
                .signWith(getSignInKey())
                .compact();
    }

    // ─── Extracción de datos ───────────────────────────────────────────────────

    /**
     * Extrae el email (subject) de un access token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el ID de usuario del subject de un refresh token.
     */
    public Long getUserIdFromRefreshToken(String refreshToken) {
        return Long.valueOf(parseClaims(refreshToken).getSubject());
    }

    // ─── Validación ────────────────────────────────────────────────────────────

    /**
     * Valida que el token sea auténtico y no haya expirado.
     *
     * @param token token JWT a validar
     * @return {@code true} si es válido, {@code false} en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Claims de JWT vacíos: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Valida que el refresh token sea auténtico y no haya expirado.
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            log.warn("Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ─── Helpers privados ──────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date expirationDesdeAhora(long millis) {
        return new Date(System.currentTimeMillis() + millis);
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}