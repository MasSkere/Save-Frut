package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.auth.LoginRequest;
import com.savefrut.api.common.dto.auth.LoginResponse;
import com.savefrut.api.common.security.UserDetailsImpl;
import com.savefrut.api.common.security.jwt.JwtTokenProvider;
import com.savefrut.api.common.security.service.UserDetailsServiceImpl;
import com.savefrut.api.common.service.RefreshTokenService;
import com.savefrut.api.common.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de autenticación — login, refresh y logout.
 *
 * <p>Usa cookies HttpOnly para el refresh token y devuelve
 * el access token JWT en el cuerpo de la respuesta.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // ─── Constantes ────────────────────────────────────────────────────────────

    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String TOKEN_COOKIE_NAME   = "token";

    // ─── Dependencias ──────────────────────────────────────────────────────────

    private final AuthenticationManager  authenticationManager;
    private final JwtTokenProvider       jwtTokenProvider;
    private final RefreshTokenService    refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    // ─── Endpoints ─────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails  = (UserDetailsImpl) authentication.getPrincipal();
        String          accessToken  = jwtTokenProvider.generateToken(authentication);
        String          refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        refreshTokenService.storeRefreshToken(userDetails.getId(), refreshToken);
        CookieUtils.agregarCookieSegura(response, REFRESH_COOKIE_NAME, refreshToken);

        log.info("Login exitoso para usuario {}", userDetails.getId());
        return ResponseEntity.ok(buildLoginResponse(accessToken, userDetails));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token no encontrado en cookie.");
        }

        String nuevoRefreshToken = refreshTokenService.validateAndRotateRefreshToken(refreshToken);
        if (nuevoRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token inválido o expirado.");
        }

        Long            userId      = jwtTokenProvider.getUserIdFromRefreshToken(nuevoRefreshToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserById(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String nuevoAccessToken = jwtTokenProvider.generateToken(authentication);
        CookieUtils.agregarCookieSegura(response, REFRESH_COOKIE_NAME, nuevoRefreshToken);

        return ResponseEntity.ok(buildLoginResponse(nuevoAccessToken, userDetails));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
                refreshTokenService.revokeRefreshToken(userId);
                log.info("Refresh token revocado para usuario {}", userId);
            } catch (Exception e) {
                log.warn("Error al revocar refresh token durante logout: {}", e.getMessage());
            }
        }

        CookieUtils.expirarCookie(response, REFRESH_COOKIE_NAME);
        CookieUtils.expirarCookie(response, TOKEN_COOKIE_NAME);

        return ResponseEntity.ok("Logout exitoso.");
    }

    // ─── Helper interno ────────────────────────────────────────────────────────

    private LoginResponse buildLoginResponse(String accessToken, UserDetailsImpl userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(accessToken, userDetails.getId(), userDetails.getUsername(), roles);
    }
}