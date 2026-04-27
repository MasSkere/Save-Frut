package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.ClienteDTO;
import com.savefrut.api.common.mapper.ClienteMapper;
import com.savefrut.api.common.repository.ClienteRepository;
import com.savefrut.api.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteDTO> getClienteLogueado(Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);

        return clienteRepository.findByUsuarioId(usuarioId)
                .map(clienteMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}