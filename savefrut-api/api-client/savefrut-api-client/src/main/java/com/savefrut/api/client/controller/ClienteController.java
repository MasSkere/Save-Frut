package com.savefrut.api.controller;

import com.savefrut.api.entity.Cliente;
import com.savefrut.api.repository.ClienteRepository;
import com.savefrut.api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint seguro para que el cliente
 * obtenga sus propios datos (para el auto-llenado).
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Cliente> getClienteLogueado(Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long usuarioId = userDetails.getId();

        return clienteRepository.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}