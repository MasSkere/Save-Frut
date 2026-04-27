package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.ComprobanteDTO;
import com.savefrut.api.common.dto.OrdenDTO;
import com.savefrut.api.common.dto.OrdenRequestDTO;
import com.savefrut.api.common.mapper.OrdenControllerMapper;
import com.savefrut.api.common.service.OrdenService;
import com.savefrut.api.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;
    private final OrdenControllerMapper ordenMapper;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrdenDTO> crearOrden(@Valid @RequestBody OrdenRequestDTO request,
                                             Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        var ordenCreada = ordenService.crearOrden(request, usuarioId);

        log.info("Orden creada con id {} para usuario {}", ordenCreada.getId(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenMapper.toDTO(ordenCreada));
    }

    @GetMapping("/comprobante/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ComprobanteDTO> getComprobante(@PathVariable Long id,
                                                     Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);

        try {
            return ResponseEntity.ok(ordenService.getDatosComprobante(id, usuarioId));
        } catch (SecurityException e) {
            log.warn("Acceso denegado a comprobante {} para usuario {}", id, usuarioId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.warn("Comprobante no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}