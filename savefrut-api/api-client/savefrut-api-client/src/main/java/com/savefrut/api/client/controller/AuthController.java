package com.savefrut.api.controller;

import com.savefrut.api.common.dto.auth.LoginRequest;
import com.savefrut.api.common.dto.auth.LoginResponse;
import com.savefrut.api.common.security.UserDetailsImpl;
import com.savefrut.api.common.security.jwt.JwtTokenProvider;
import com.savefrut.api.common.service.RefreshTokenService;
import com.savefrut.api.common.security.service.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // Permite que el cliente web (en otro dominio) llame a esta API
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Este es el endpoint que el JavaScript del cliente llamará.
     * Recibe el JSON (LoginRequest) y devuelve el JSON (LoginResponse).
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        // 1. Spring Security autentica al usuario usando el email y password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // 2. Si es exitoso, establecemos la autenticación en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Obtenemos el "UserDetails" que creamos (para sacar el ID, email, roles)
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 4. Generamos el "pase VIP" (Token JWT)
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 5. Generamos el refresh token
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        // 6. Almacenamos el refresh token en Redis
        refreshTokenService.storeRefreshToken(userDetails.getId(), refreshToken);

        // 7. Obtenemos la lista de roles (ej. "ROLE_CLIENTE")
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 8. Configuramos la cookie httpOnly con el refresh token
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // Solo se enviará por HTTPS
        refreshCookie.setPath("/"); // Disponible en toda la aplicación
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días en segundos
        response.addCookie(refreshCookie);

        // 9. Respondemos con el JSON que contiene el access token y los datos del usuario
        return ResponseEntity.ok(new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(), // getUsername() devuelve el email, como lo configuramos
                roles
        ));
    }

    /**
     * Endpoint para refrescar el access token usando el refresh token de la cookie.
     * El refresh token se lee automáticamente de la cookie httpOnly.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshTokenFromCookie,
                                         HttpServletResponse response) {

        if (refreshTokenFromCookie == null || refreshTokenFromCookie.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token no encontrado en cookie");
        }

        // Validar y rotar el refresh token
        String newRefreshToken = refreshTokenService.validateAndRotateRefreshToken(refreshTokenFromCookie);

        if (newRefreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token inválido o expirado");
        }

        // Extraer userId del nuevo refresh token
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(newRefreshToken);

        // Cargar detalles del usuario
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserById(userId);

        // Crear Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        // Generar nuevo access token
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        // Actualizar cookie del refresh token
        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días
        response.addCookie(refreshCookie);

        // Obtener roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Responder con nuevo access token
        return ResponseEntity.ok(new LoginResponse(
                newAccessToken,
                userDetails.getId(),
                userDetails.getUsername(),
                roles
        ));
    }

    /**
     * Endpoint para logout (revocar refresh token).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refresh_token", required = false) String refreshTokenFromCookie,
                                   HttpServletResponse response) {

        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isEmpty()) {
            try {
                Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshTokenFromCookie);
                refreshTokenService.revokeRefreshToken(userId);
            } catch (Exception e) {
                // Ignorar errores al revocar
            }
        }

        // Limpiar cookies
        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // Expirar inmediatamente
        response.addCookie(refreshCookie);

        Cookie tokenCookie = new Cookie("token", "");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        return ResponseEntity.ok("Logout exitoso");
    }

    // Aquí iría el endpoint de @PostMapping("/registro") si quisiéramos
}
