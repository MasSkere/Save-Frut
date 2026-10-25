package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.dto.AlertaDTO;
import com.savefrut.mscatalogo.dto.ResumenProductosDTO;
import com.savefrut.mscatalogo.dto.vendedor.ProductoVendedorDTO;
import com.savefrut.mscatalogo.service.ResumenProductosService;
import com.savefrut.mscatalogo.dto.vendedor.ProductoVendedorRequest;
import com.savefrut.mscatalogo.service.ProductoVendedorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendedor/productos")
@RequiredArgsConstructor
public class ProductoVendedorController {

    private final ProductoVendedorService productoVendedorService;
    private final ResumenProductosService resumenProductosService;

    @GetMapping("/resumen")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ResumenProductosDTO> resumen(HttpServletRequest request) {
        return vendedorDe(request)
                .map(vendedorId -> ResponseEntity.ok(resumenProductosService.getResumen(vendedorId)))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<AlertaDTO>> alertas(HttpServletRequest request) {
        return vendedorDe(request)
                .map(vendedorId -> ResponseEntity.ok(resumenProductosService.getAlertas(vendedorId)))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoVendedorDTO> cambiarActivo(@PathVariable Long id,
                                                            @RequestParam Boolean activo,
                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return productoVendedorService.cambiarActivo(id, userId, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private java.util.Optional<Long> vendedorDe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return java.util.Optional.empty();
        }
        return productoVendedorService.getVendedor(userId).map(v -> v.id());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public ResponseEntity<List<ProductoVendedorDTO>> listar(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size,
                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var vendedorOpt = productoVendedorService.getVendedor(userId);
        if (vendedorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(productoVendedorService.getProductos(vendedorOpt.get().id(), page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoVendedorDTO> crear(@Valid @RequestBody ProductoVendedorRequest req,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productoVendedorService.crearProducto(userId, req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoVendedorDTO> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ProductoVendedorRequest req,
                                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return productoVendedorService.actualizarProducto(id, userId, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoVendedorDTO> ajustarStock(@PathVariable Long id,
                                                           @RequestParam Integer stock,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return productoVendedorService.ajustarStock(id, userId, stock)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoVendedorDTO> darDeBaja(@PathVariable Long id,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return productoVendedorService.cambiarActivo(id, userId, Boolean.FALSE)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
