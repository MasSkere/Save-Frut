package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.dto.ProductoDTO;
import com.savefrut.mscatalogo.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
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

    @GetMapping("/vendedor/{vendedorId}")
    public List<ProductoDTO> getProductosByVendedor(@PathVariable Long vendedorId) {
        return productoService.getProductosByVendedor(vendedorId);
    }

    @GetMapping("/buscar")
    public List<ProductoDTO> buscarProductos(@RequestParam("term") String termino) {
        return productoService.buscarProductosPorNombre(termino);
    }
}
