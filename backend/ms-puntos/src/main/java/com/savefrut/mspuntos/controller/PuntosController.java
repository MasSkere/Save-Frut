package com.savefrut.mspuntos.controller;

import com.savefrut.mspuntos.dto.EstadisticasPuntosDTO;
import com.savefrut.mspuntos.dto.MisPuntosDTO;
import com.savefrut.mspuntos.feign.IdentidadFeignClient;
import com.savefrut.mspuntos.service.PuntosService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de Eco-Puntos. La acumulación ocurre de forma asíncrona en {@code OrdenPagadaConsumer}.
 */
@RestController
@RequestMapping("/puntos")
@RequiredArgsConstructor
public class PuntosController {

    private final PuntosService puntosService;
    private final IdentidadFeignClient identidadFeignClient;

    @GetMapping("/mis-puntos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<MisPuntosDTO> misPuntos(@AuthenticationPrincipal Long usuarioId) {
        try {
            Long clienteId = identidadFeignClient.clientePorUsuario(usuarioId).id();
            return ResponseEntity.ok(puntosService.getMisPuntos(clienteId));
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public EstadisticasPuntosDTO estadisticas() {
        return puntosService.getEstadisticas();
    }
}
