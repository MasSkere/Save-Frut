package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.OrdenAdminDTO;
import com.savefrut.api.admin.service.OrdenAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrdenAdminController {

    private final OrdenAdminService ordenService;

    @GetMapping
    public ResponseEntity<List<OrdenAdminDTO>> getOrdenes(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(ordenService.getOrdenes(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenAdminDTO> getOrdenById(@PathVariable Long id) {
        OrdenAdminDTO orden = ordenService.getOrdenById(id);
        return orden != null
                ? ResponseEntity.ok(orden)
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenAdminDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        OrdenAdminDTO orden = ordenService.cambiarEstado(id, estado);
        return orden != null
                ? ResponseEntity.ok(orden)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<OrdenAdminDTO> cancelarOrden(
            @PathVariable Long id,
            @RequestBody CancelarRequest request) {
        OrdenAdminDTO orden = ordenService.cancelarOrden(id, request.razon());
        return orden != null
                ? ResponseEntity.ok(orden)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> getEstadisticas() {
        return ResponseEntity.ok(ordenService.getEstadisticas());
    }

    public record CancelarRequest(String razon) {}
}