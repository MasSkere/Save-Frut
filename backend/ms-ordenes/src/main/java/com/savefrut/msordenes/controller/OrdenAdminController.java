package com.savefrut.msordenes.controller;

import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.dto.admin.DashboardAdminDTO;
import com.savefrut.msordenes.dto.admin.ReporteVentasDTO;
import com.savefrut.msordenes.service.DashboardAdminService;
import com.savefrut.msordenes.service.OrdenAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrdenAdminController {

    private final OrdenAdminService ordenAdminService;
    private final DashboardAdminService dashboardAdminService;

    @GetMapping("/ordenes")
    public List<OrdenDTO> listar(@RequestParam(required = false) String estado) {
        return ordenAdminService.getOrdenes(estado);
    }

    @GetMapping("/ordenes/{id}")
    public OrdenDTO obtener(@PathVariable Long id) {
        return ordenAdminService.getOrdenById(id);
    }

    @PatchMapping("/ordenes/{id}/estado")
    public OrdenDTO cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ordenAdminService.cambiarEstado(id, estado);
    }

    @PostMapping("/ordenes/{id}/cancelar")
    public OrdenDTO cancelar(@PathVariable Long id, @RequestParam(required = false) String razon) {
        return ordenAdminService.cancelarOrden(id, razon);
    }

    @GetMapping("/dashboard")
    public DashboardAdminDTO dashboard() {
        return dashboardAdminService.getDashboard();
    }

    @GetMapping("/reportes/ventas")
    public ReporteVentasDTO reporteVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(29);
        return dashboardAdminService.getReporteVentas(inicio, fin);
    }
}
