package com.savefrut.api.controller;

import com.savefrut.api.dto.auth.LoginRequest;
import com.savefrut.api.dto.auth.LoginResponse;
import com.savefrut.api.security.UserDetailsImpl;
import com.savefrut.api.security.jwt.JwtTokenProvider;
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

    /**
     * Este es el endpoint que el JavaScript del cliente llamará.
     * Recibe el JSON (LoginRequest) y devuelve el JSON (LoginResponse).
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. Spring Security autentica al usuario usando el email y password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // 2. Si es exitoso, establecemos la autenticación en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Obtenemos el "UserDetails" que creamos (para sacar el ID, email, roles)
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 4. Generamos el "pase VIP" (Token JWT)
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 5. Obtenemos la lista de roles (ej. "ROLE_CLIENTE")
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 6. Respondemos con el JSON que contiene el token y los datos del usuario
        return ResponseEntity.ok(new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(), // getUsername() devuelve el email, como lo configuramos
                roles
        ));
    }

    // Aquí iría el endpoint de @PostMapping("/registro") si quisiéramos
}

