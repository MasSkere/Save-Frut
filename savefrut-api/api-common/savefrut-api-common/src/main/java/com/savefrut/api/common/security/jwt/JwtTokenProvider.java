package com.savefrut.api.common.security.jwt;

import com.savefrut.api.common.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Esta clase es el "cerebro" de JWT.
 * Se encarga de:
 * 1. Generar nuevos tokens (el "pase VIP").
 * 2. Validar tokens existentes (el "sello holográfico").
 * 3. Extraer información del token (ej. el email).
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // 1. Clave Secreta: Es la "firma" de nuestros tokens.
    // DEBE ser larga y segura. La ponemos en 'application.properties'.
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // 2. Tiempo de Expiración: Cuánto dura el "pase VIP".
    // También la ponemos en 'application.properties'.
    @Value("${app.jwt.expiration-minutes}")
    private int jwtExpirationMinutes;

    // 3. Tiempo de Expiración para Refresh Token
    @Value("${app.jwt.refresh.expiration-days}")
    private int jwtRefreshExpirationDays;

    /**
     * Genera un nuevo token JWT para un usuario que se ha autenticado exitosamente.
     * @param authentication El objeto Authentication de Spring Security (contiene el UserDetailsImpl).
     * @return Un String que es el Token JWT.
     */
    public String generateToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        // Obtenemos los roles del usuario (ej. "ROLE_CLIENTE")
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                // "Subject" (Asunto): El email del usuario
                .subject(userPrincipal.getUsername()) 
                .issuedAt(now)
                .expiration(expiryDate)
                // "Claim" (Dato extra): Guardamos los roles dentro del token
                .claim("roles", roles) 
                // "Claim" (Dato extra): Guardamos el ID de usuario
                .claim("userId", userPrincipal.getId()) 
                // Firma: Lo firmamos con nuestra clave secreta usando el algoritmo HS512
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Extrae el email (el "Subject") del token JWT.
     * @param token El String del token.
     * @return El email del usuario.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }

    /**
     * Valida si un token es auténtico (comprueba la firma y la expiración).
     * @param token El String del token.
     * @return true si es válido, false si no.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Claims de JWT están vacíos: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Validación de JWT falló: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Genera un refresh token (más simple, solo userId y expiración larga).
     * @param userId El ID del usuario.
     * @return Un String que es el Refresh Token JWT.
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) jwtRefreshExpirationDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId)) // Solo el userId como subject
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Extrae el userId del refresh token.
     * @param refreshToken El refresh token.
     * @return El userId del usuario.
     */
    public Long getUserIdFromRefreshToken(String refreshToken) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    /**
     * Valida si un refresh token es válido.
     * @param refreshToken El refresh token.
     * @return true si es válido, false si no.
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            logger.error("Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Método privado para obtener la clave secreta (SecretKey) a partir del String.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}