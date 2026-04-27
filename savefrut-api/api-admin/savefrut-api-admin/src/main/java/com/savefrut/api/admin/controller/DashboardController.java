package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.*;
import com.savefrut.api.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricasDTO> getMetricas() {
        return ResponseEntity.ok(dashboardService.getMetricas());
    }

    @GetMapping("/ventas-semanales")
    public ResponseEntity<List<VentaDiariaDTO>> getVentasSemanales() {
        return ResponseEntity.ok(dashboardService.getVentasSemanales());
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<ProductoVendidoDTO>> getTopProductos() {
        return ResponseEntity.ok(dashboardService.getTopProductos());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaDTO>> getAlertas() {
        return ResponseEntity.ok(dashboardService.getAlertas());
    }
}