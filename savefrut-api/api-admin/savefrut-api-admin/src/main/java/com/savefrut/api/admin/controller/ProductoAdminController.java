package com.savefrut.api.admin.controller;

import com.savefrut.api.admin.dto.ProductoAdminDTO;
import com.savefrut.api.admin.dto.ProductoAdminRequest;
import com.savefrut.api.admin.service.ProductoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductoAdminController {

    private final ProductoAdminService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoAdminDTO>> getProductos(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) Boolean stockBajo) {
        return ResponseEntity.ok(productoService.getProductos(categoriaId, vendedorId, stockBajo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoAdminDTO> getProductoById(@PathVariable Long id) {
        ProductoAdminDTO producto = productoService.getProductoById(id);
        return producto != null
                ? ResponseEntity.ok(producto)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductoAdminDTO> crearProducto(
            @Valid @RequestBody ProductoAdminRequest request) {
        return ResponseEntity.ok(productoService.crearProducto(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoAdminDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoAdminRequest request) {
        ProductoAdminDTO producto = productoService.actualizarProducto(id, request);
        return producto != null
                ? ResponseEntity.ok(producto)
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoAdminDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        ProductoAdminDTO producto = productoService.ajustarStock(id, stock);
        return producto != null
                ? ResponseEntity.ok(producto)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}