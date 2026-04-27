package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.UsuarioAdminDTO;
import com.savefrut.api.admin.service.UsuarioAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioAdminDTO>> getUsuarios(
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(usuarioService.getUsuarios(tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioAdminDTO> getUsuarioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioAdminDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody EstadoRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.cambiarEstadoUsuario(id, request.estado(), request.razon()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public record EstadoRequest(String estado, String razon) {}
}