package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.ProductoDTO;
import com.savefrut.api.common.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público de productos.
 * Delega toda la lógica a {@link ProductoService}.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> getProductosDisponibles() {
        return productoService.getProductosDisponibles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id).stream()
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{id}")
    public List<ProductoDTO> getProductosByCategoria(@PathVariable Long id) {
        return productoService.getProductosByCategoria(id);
    }

    @GetMapping("/buscar")
    public List<ProductoDTO> buscarProductos(@RequestParam("term") String termino) {
        return productoService.buscarProductosPorNombre(termino);
    }
}