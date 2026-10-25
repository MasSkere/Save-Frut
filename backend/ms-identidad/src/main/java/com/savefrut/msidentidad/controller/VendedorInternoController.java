package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.ClienteDTO;
import com.savefrut.msidentidad.dto.EstadisticasDTO;
import com.savefrut.msidentidad.dto.VendedorDTO;
import com.savefrut.msidentidad.mapper.ClienteMapper;
import com.savefrut.msidentidad.mapper.VendedorMapper;
import com.savefrut.msidentidad.repository.ClienteRepository;
import com.savefrut.msidentidad.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints internos consumidos por otros microservicios vía Feign (no expuestos en el API Gateway).
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class VendedorInternoController {

    private final VendedorRepository vendedorRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorMapper vendedorMapper;
    private final ClienteMapper clienteMapper;

    @GetMapping("/vendedores")
    @Transactional(readOnly = true)
    public List<VendedorDTO> listarVendedores() {
        return vendedorRepository.findAll().stream()
                .map(vendedorMapper::toDTO)
                .toList();
    }

    @GetMapping("/vendedores/usuario/{usuarioId}")
    @Transactional(readOnly = true)
    public ResponseEntity<VendedorDTO> vendedorPorUsuario(@PathVariable Long usuarioId) {
        return vendedorRepository.findByUsuarioId(usuarioId)
                .map(vendedorMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clientes/usuario/{usuarioId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ClienteDTO> clientePorUsuario(@PathVariable Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .map(clienteMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clientes")
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toDTO)
                .toList();
    }

    @GetMapping("/estadisticas")
    @Transactional(readOnly = true)
    public EstadisticasDTO estadisticas() {
        long aprobados = vendedorRepository.countByAprobadoTrue();
        return new EstadisticasDTO(clienteRepository.count(), aprobados, vendedorRepository.count() - aprobados);
    }
}
