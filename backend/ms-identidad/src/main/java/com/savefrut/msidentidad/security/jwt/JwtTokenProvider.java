package com.savefrut.msidentidad.security.jwt;

import com.savefrut.msidentidad.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLES   = "roles";
    private static final String CLAIM_USER_ID = "userId";
    private static final long   MILLIS_PER_MINUTE = 60_000L;
    private static final long   MILLIS_PER_DAY    = 86_400_000L;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_EXPIRATION_MINUTES}")
    private int accessExpirationMinutes;

    @Value("${JWT_REFRESH_EXPIRATION_DAYS}")
    private int refreshExpirationDays;

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

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(expirationDesdeAhora(refreshExpirationDays * MILLIS_PER_DAY))
                .signWith(getSignInKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        return Long.valueOf(parseClaims(refreshToken).getSubject());
    }

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

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            log.warn("Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

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
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
