package com.savefrut.msordenes.controller;

import com.savefrut.msordenes.dto.vendedor.OrdenVendedorDTO;
import com.savefrut.msordenes.dto.vendedor.ResumenVendedorDTO;
import com.savefrut.msordenes.service.OrdenVendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendedor/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class OrdenVendedorController {

    private final OrdenVendedorService ordenVendedorService;

    @GetMapping
    public List<OrdenVendedorDTO> listar(@RequestParam(required = false) String estado,
                                         @AuthenticationPrincipal Long usuarioId) {
        return ordenVendedorService.getOrdenes(usuarioId, estado);
    }

    @GetMapping("/resumen")
    public ResumenVendedorDTO resumen(@AuthenticationPrincipal Long usuarioId) {
        return ordenVendedorService.getResumen(usuarioId);
    }

    @PatchMapping("/{id}/preparar")
    public OrdenVendedorDTO marcarPreparado(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ordenVendedorService.marcarPreparado(id, usuarioId);
    }
}
