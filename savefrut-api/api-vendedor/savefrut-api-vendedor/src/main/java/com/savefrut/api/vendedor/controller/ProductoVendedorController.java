package com.savefrut.api.vendedor.controller;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.vendedor.service.ProductoVendedorService;
import com.savefrut.api.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedor/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class ProductoVendedorController {

    private final ProductoVendedorService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoVendedorDTO>> getProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        var vendedor = productoService.getVendedor(usuarioId);
        if (vendedor.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ProductoVendedorDTO> productos = productoService.getProductos(vendedor.get().getVendedorId(), page, size);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoVendedorDTO> getProducto(
            @PathVariable Long id,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        var vendedor = productoService.getVendedor(usuarioId);
        if (vendedor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return productoService.getProductoById(id, vendedor.get().getVendedorId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoVendedorDTO> crearProducto(
            @Valid @RequestBody ProductoVendedorRequest request,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return ResponseEntity.ok(productoService.crearProducto(usuarioId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoVendedorDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoVendedorRequest request,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return productoService.actualizarProducto(id, usuarioId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoVendedorDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer stock,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return productoService.ajustarStock(id, usuarioId, stock)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoVendedorDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo,
            Authentication authentication) {
        Long usuarioId = SecurityUtils.extraerUsuarioId(authentication);
        return productoService.cambiarActivo(id, usuarioId, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}