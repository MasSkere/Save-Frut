package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.ClienteDTO;
import com.savefrut.msidentidad.dto.ClienteRequest;
import com.savefrut.msidentidad.mapper.ClienteMapper;
import com.savefrut.msidentidad.repository.ClienteRepository;
import com.savefrut.msidentidad.repository.DistritoRepository;
import com.savefrut.msidentidad.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final DistritoRepository distritoRepository;
    private final ClienteMapper clienteMapper;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ClienteDTO> getClienteLogueado(Authentication authentication) {
        return clienteRepository.findByUsuarioId(SecurityUtils.extraerUsuarioId(authentication))
                .map(clienteMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<ClienteDTO> actualizar(Authentication authentication, @RequestBody ClienteRequest request) {
        return clienteRepository.findByUsuarioId(SecurityUtils.extraerUsuarioId(authentication))
                .map(cliente -> {
                    if (request.nombres() != null) cliente.setNombres(request.nombres());
                    if (request.apellidos() != null) cliente.setApellidos(request.apellidos());
                    if (request.telefono() != null) cliente.setTelefono(request.telefono());
                    if (request.direccion() != null) cliente.setDireccion(request.direccion());
                    if (request.latitud() != null) cliente.setLatitud(request.latitud());
                    if (request.longitud() != null) cliente.setLongitud(request.longitud());
                    if (request.distritoId() != null) {
                        distritoRepository.findById(request.distritoId()).ifPresent(cliente::setDistrito);
                    }
                    return clienteMapper.toDTO(clienteRepository.save(cliente));
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
