package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.ProductoVencidoDTO;
import com.savefrut.api.admin.dto.ReporteVentasDTO;
import com.savefrut.api.admin.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasDTO> getReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.getReporteVentas(desde, hasta));
    }

    @GetMapping("/productos-vencidos")
    public ResponseEntity<List<ProductoVencidoDTO>> getProductosVencidos() {
        return ResponseEntity.ok(reporteService.getProductosVencidos());
    }
}