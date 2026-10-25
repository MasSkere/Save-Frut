package com.savefrut.msordenes.controller;

import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.dto.OrdenRequestDTO;
import com.savefrut.msordenes.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class OrdenController {

    private final OrdenService ordenService;

    @PostMapping
    public ResponseEntity<OrdenDTO> crearOrden(@Valid @RequestBody OrdenRequestDTO request,
                                               @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crearOrden(request, usuarioId));
    }

    @GetMapping("/mis-ordenes")
    public List<OrdenDTO> misOrdenes(@AuthenticationPrincipal Long usuarioId) {
        return ordenService.misOrdenes(usuarioId);
    }

    @GetMapping("/{id}")
    public OrdenDTO getOrden(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ordenService.detalleParaCliente(id, usuarioId);
    }
}
