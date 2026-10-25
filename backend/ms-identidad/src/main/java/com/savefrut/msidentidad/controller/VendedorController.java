package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.VendedorDTO;
import com.savefrut.msidentidad.mapper.VendedorMapper;
import com.savefrut.msidentidad.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Directorio público de tiendas aprobadas.
 */
@RestController
@RequestMapping("/vendedores")
@RequiredArgsConstructor
public class VendedorController {

    private final VendedorRepository vendedorRepository;
    private final VendedorMapper vendedorMapper;

    @GetMapping
    @Transactional(readOnly = true)
    public List<VendedorDTO> getVendedores() {
        return vendedorRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getAprobado()))
                .map(vendedorMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<VendedorDTO> getVendedorById(@PathVariable Long id) {
        return vendedorRepository.findById(id)
                .filter(v -> Boolean.TRUE.equals(v.getAprobado()))
                .map(vendedorMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
