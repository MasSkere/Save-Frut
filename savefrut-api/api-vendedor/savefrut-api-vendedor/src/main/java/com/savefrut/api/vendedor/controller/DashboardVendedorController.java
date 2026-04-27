package com.savefrut.api.vendedor.controller;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.vendedor.service.DashboardVendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedor/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class DashboardVendedorController {

    private final DashboardVendedorService dashboardService;

    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricasDTO> getMetricas(@RequestAttribute Long vendedorId) {
        return ResponseEntity.ok(dashboardService.getMetricas(vendedorId));
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<VentaDiariaDTO>> getVentas(@RequestAttribute Long vendedorId) {
        return ResponseEntity.ok(dashboardService.getVentasSemanales(vendedorId));
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<ProductoVendedorDTO>> getTopProductos(@RequestAttribute Long vendedorId) {
        return ResponseEntity.ok(dashboardService.getTopProductos(vendedorId));
    }
}