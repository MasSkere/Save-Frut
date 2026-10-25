package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.vendedor.PerfilVendedorDTO;
import com.savefrut.msidentidad.dto.vendedor.PerfilVendedorRequest;
import com.savefrut.msidentidad.service.PerfilVendedorService;
import com.savefrut.msidentidad.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/perfil")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class PerfilVendedorController {

    private final PerfilVendedorService perfilVendedorService;

    @GetMapping
    public ResponseEntity<PerfilVendedorDTO> miPerfil(Authentication auth) {
        return perfilVendedorService.getPerfil(SecurityUtils.extraerUsuarioId(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<PerfilVendedorDTO> actualizar(Authentication auth,
                                                       @RequestBody PerfilVendedorRequest request) {
        return perfilVendedorService.actualizarPerfil(SecurityUtils.extraerUsuarioId(auth), request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
