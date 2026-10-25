package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.dto.AlertaDTO;
import com.savefrut.mscatalogo.dto.ResumenProductosDTO;
import com.savefrut.mscatalogo.dto.admin.ProductoAdminDTO;
import com.savefrut.mscatalogo.service.ResumenProductosService;
import com.savefrut.mscatalogo.dto.admin.ProductoAdminRequest;
import com.savefrut.mscatalogo.service.ProductoAdminService;
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

    private final ProductoAdminService productoAdminService;
    private final ResumenProductosService resumenProductosService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenProductosDTO> resumen() {
        return ResponseEntity.ok(resumenProductosService.getResumen(null));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaDTO>> alertas() {
        return ResponseEntity.ok(resumenProductosService.getAlertas(null));
    }

    @GetMapping
    public ResponseEntity<List<ProductoAdminDTO>> listar() {
        return ResponseEntity.ok(productoAdminService.getProductos(null, null, null));
    }

    @PostMapping
    public ResponseEntity<ProductoAdminDTO> crear(@RequestBody ProductoAdminRequest request) {
        return ResponseEntity.ok(productoAdminService.crearProducto(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoAdminDTO> actualizar(@PathVariable Long id,
                                                      @RequestBody ProductoAdminRequest request) {
        ProductoAdminDTO actualizado = productoAdminService.actualizarProducto(id, request);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        productoAdminService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
