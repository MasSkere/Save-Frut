package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.admin.UsuarioAdminDTO;
import com.savefrut.msidentidad.service.UsuarioAdminService;
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

    private final UsuarioAdminService usuarioAdminService;

    @GetMapping
    public ResponseEntity<List<UsuarioAdminDTO>> listar(@RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(usuarioAdminService.getUsuarios(tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioAdminDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioAdminService.getUsuarioById(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioAdminDTO> cambiarEstado(@PathVariable Long id,
                                                        @RequestParam String estado,
                                                        @RequestParam(required = false) String razon) {
        return ResponseEntity.ok(usuarioAdminService.cambiarEstadoUsuario(id, estado, razon));
    }
}
