package com.savefrut.api.common.security.jwt;

import com.savefrut.api.common.security.exception.InvalidJwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Filtro de seguridad JWT que intercepta cada petición HTTP exactamente una vez.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Extraer el token JWT del header Authorization o de la cookie.</li>
 *   <li>Validar el token y autenticar al usuario en el SecurityContext.</li>
 * </ul>
 *
 * <p>Prioridad de extracción: Header {@code Authorization: Bearer <token>} > Cookie {@code token}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    // ─── Constantes ────────────────────────────────────────────────────────────

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";
    private static final int    BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private static final String JWT_COOKIE_NAME      = "token";

    // ─── Dependencias (inyección por constructor via @RequiredArgsConstructor) ──

    private final JwtTokenProvider   jwtTokenProvider;
    private final UserDetailsService  userDetailsService;   // interfaz, no la implementación

    // ─── Lógica principal ──────────────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain) throws ServletException, IOException {

        extractToken(request)
                .filter(jwtTokenProvider::validateToken)
                .ifPresent(token -> authenticateUser(token, request));

        filterChain.doFilter(request, response);
    }

    // ─── Extracción del token ──────────────────────────────────────────────────

    /**
     * Intenta obtener el token JWT del header o de la cookie.
     *
     * @param request petición HTTP entrante
     * @return el token si existe, {@link Optional#empty()} en caso contrario
     */
    private Optional<String> extractToken(HttpServletRequest request) {
        return extractFromHeader(request)
                .or(() -> extractFromCookie(request));
    }

    private Optional<String> extractFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(headerValue) && headerValue.startsWith(BEARER_PREFIX)) {
            return Optional.of(headerValue.substring(BEARER_PREFIX_LENGTH));
        }
        return Optional.empty();
    }

    private Optional<String> extractFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> JWT_COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }

    // ─── Autenticación ─────────────────────────────────────────────────────────

    /**
     * Carga el usuario desde el token y lo registra en el {@link SecurityContextHolder}.
     *
     * @param token   token JWT ya validado
     * @param request petición HTTP actual
     * @throws InvalidJwtAuthenticationException si el usuario no puede ser cargado
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            String      username    = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Usuario '{}' autenticado correctamente.", username);

        } catch (Exception ex) {
            log.warn("No se pudo autenticar al usuario con el token provisto: {}", ex.getMessage());
            // No relanzamos: simplemente el SecurityContext queda vacío
            // y el request continuará sin autenticación (denegado por los filtros de autorización).
        }
    }
}