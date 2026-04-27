package com.savefrut.api.vendedor.controller;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.vendedor.service.PerfilVendedorService;
import com.savefrut.api.common.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendedor/perfil")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class PerfilVendedorController {

    private final PerfilVendedorService perfilService;

    @GetMapping
    public ResponseEntity<PerfilVendedorDTO> getPerfil(Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return perfilService.getPerfil(usuarioId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
    }

    @PutMapping
    public ResponseEntity<PerfilVendedorDTO> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody PerfilVendedorRequest request) {
        
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return perfilService.actualizarPerfil(usuarioId, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
    }
}