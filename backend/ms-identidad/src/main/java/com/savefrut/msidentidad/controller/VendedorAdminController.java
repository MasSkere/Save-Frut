package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.admin.VendedorAdminDTO;
import com.savefrut.msidentidad.service.VendedorAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vendedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VendedorAdminController {

    private final VendedorAdminService vendedorAdminService;

    @GetMapping
    public ResponseEntity<List<VendedorAdminDTO>> listar() {
        return ResponseEntity.ok(vendedorAdminService.getVendedores());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<VendedorAdminDTO>> pendientes() {
        return ResponseEntity.ok(vendedorAdminService.getVendedoresPendientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorAdminDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(vendedorAdminService.getVendedorById(id));
    }

    @PatchMapping("/{id}/aprobacion")
    public ResponseEntity<VendedorAdminDTO> aprobarRechazar(@PathVariable Long id,
                                                           @RequestParam boolean aprobado,
                                                           @RequestParam(required = false) String razon) {
        return ResponseEntity.ok(vendedorAdminService.cambiarEstadoAprobacion(id, aprobado, razon));
    }
}
