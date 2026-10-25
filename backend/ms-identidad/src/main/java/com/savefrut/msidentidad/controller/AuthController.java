package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.LoginRequest;
import com.savefrut.msidentidad.dto.LoginResponse;
import com.savefrut.msidentidad.dto.RegistroRequest;
import com.savefrut.msidentidad.entity.Usuario;
import com.savefrut.msidentidad.repository.ClienteRepository;
import com.savefrut.msidentidad.repository.VendedorRepository;
import com.savefrut.msidentidad.service.RegistroService;
import com.savefrut.msidentidad.security.UserDetailsImpl;
import com.savefrut.msidentidad.security.jwt.JwtTokenProvider;
import com.savefrut.msidentidad.security.service.UserDetailsServiceImpl;
import com.savefrut.msidentidad.service.RefreshTokenService;
import com.savefrut.msidentidad.util.CookieUtils;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final RegistroService registroService;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = registroService.registrar(request);
        String mensaje = "VENDEDOR".equals(request.rol())
                ? "Registro exitoso. Tu tienda quedará visible cuando un administrador la apruebe."
                : "Registro exitoso. Ya puedes iniciar sesión.";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", usuario.getId(), "email", usuario.getEmail(), "message", mensaje));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        refreshTokenService.storeRefreshToken(userDetails.getId(), refreshToken);
        CookieUtils.agregarCookieSegura(response, REFRESH_COOKIE_NAME, refreshToken);

        log.info("Login exitoso para usuario {}", userDetails.getId());
        return ResponseEntity.ok(buildLoginResponse(accessToken, userDetails));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
                                     HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token no encontrado en cookie.");
        }

        String nuevoRefreshToken = refreshTokenService.validateAndRotateRefreshToken(refreshToken);
        if (nuevoRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido o expirado.");
        }

        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(nuevoRefreshToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserById(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String nuevoAccessToken = jwtTokenProvider.generateToken(authentication);
        CookieUtils.agregarCookieSegura(response, REFRESH_COOKIE_NAME, nuevoRefreshToken);

        return ResponseEntity.ok(buildLoginResponse(nuevoAccessToken, userDetails));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
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
        return ResponseEntity.ok("Logout exitoso.");
    }

    private LoginResponse buildLoginResponse(String accessToken, UserDetailsImpl userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        LoginResponse loginResponse = new LoginResponse(accessToken, userDetails.getId(), userDetails.getUsername(), roles);
        loginResponse.setNombre(obtenerNombre(userDetails.getId(), userDetails.getUsername()));
        return loginResponse;
    }

    private String obtenerNombre(Long usuarioId, String email) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .map(c -> c.getNombres() + " " + c.getApellidos())
                .or(() -> vendedorRepository.findByUsuarioId(usuarioId).map(v -> v.getRazonSocial()))
                .orElse(email);
    }
}
