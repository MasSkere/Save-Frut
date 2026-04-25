package com.savefrut.api.controller;

import com.savefrut.api.dto.ComprobanteDTO;
import com.savefrut.api.dto.OrdenRequestDTO; // ¡Import cambiado!
import com.savefrut.api.entity.Orden;
import com.savefrut.api.security.UserDetailsImpl;
import com.savefrut.api.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    /**
     * ENDPOINT: POST /api/ordenes
     * Crea una nueva orden (Checkout).
     * ¡Está protegido! Solo 'ROLE_CLIENTE' puede entrar.
     * Llama al nuevo OrdenService que recalcula el total.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Orden> crearOrden(@Valid @RequestBody OrdenRequestDTO ordenRequest,
                                            Authentication authentication) {
        
        // 1. Obtenemos el ID del usuario desde el Token JWT
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long usuarioId = userDetails.getId();
        
        // 2. Llamamos al servicio con el DTO del cliente y el ID seguro
        Orden nuevaOrden = ordenService.crearOrden(ordenRequest, usuarioId);
        
        // 3. Respondemos con la orden creada
        return ResponseEntity.status(201).body(nuevaOrden);
    }
    
    @GetMapping("/comprobante/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ComprobanteDTO> getComprobante(@PathVariable Long id, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long usuarioId = userDetails.getId();
        
        try {
            // Llama a la nueva función del servicio
            ComprobanteDTO comprobante = ordenService.getDatosComprobante(id, usuarioId);
            return ResponseEntity.ok(comprobante);
        } catch (SecurityException e) {
            // Si la orden no es del usuario
            return ResponseEntity.status(403).build(); // 403 Forbidden
        } catch (Exception e) {
            // Si la orden no existe
            return ResponseEntity.status(404).build(); // 404 Not Found
        }
    }
}