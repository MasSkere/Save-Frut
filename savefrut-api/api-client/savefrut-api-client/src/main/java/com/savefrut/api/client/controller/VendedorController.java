package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.ProductoDTO;
import com.savefrut.api.common.dto.VendedorDTO;
import com.savefrut.api.common.mapper.VendedorMapper;
import com.savefrut.api.common.repository.VendedorRepository;
import com.savefrut.api.common.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class VendedorController {

    private final VendedorRepository vendedorRepository;
    private final ProductoService productoService;
    private final VendedorMapper vendedorMapper;

    @GetMapping
    public List<VendedorDTO> getVendedores() {
        return vendedorRepository.findAll().stream()
                .map(vendedorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorDTO> getVendedorById(@PathVariable Long id) {
        return vendedorRepository.findById(id)
                .map(vendedorMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/productos")
    public List<ProductoDTO> getProductosPorVendedor(@PathVariable Long id) {
        return productoService.getProductosByVendedor(id);
    }
}