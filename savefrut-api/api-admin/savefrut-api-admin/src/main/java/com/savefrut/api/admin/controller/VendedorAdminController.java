package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.VendedorAdminDTO;
import com.savefrut.api.admin.service.VendedorAdminService;
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

    private final VendedorAdminService vendedorService;

    @GetMapping("/pendientes")
    public ResponseEntity<List<VendedorAdminDTO>> getVendedoresPendientes() {
        return ResponseEntity.ok(vendedorService.getVendedoresPendientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorAdminDTO> getVendedorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vendedorService.getVendedorById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/aprobacion")
    public ResponseEntity<VendedorAdminDTO> cambiarAprobacion(
            @PathVariable Long id,
            @RequestBody AprobacionRequest request) {
        try {
            return ResponseEntity.ok(vendedorService.cambiarEstadoAprobacion(id, request.aprobado(), request.razon()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public record AprobacionRequest(Boolean aprobado, String razon) {}
}