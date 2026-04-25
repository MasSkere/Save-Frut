package com.savefrut.api.controller;

import com.savefrut.api.dto.ProductoDTO;
import com.savefrut.api.entity.Vendedor;
import com.savefrut.api.repository.VendedorRepository;
import com.savefrut.api.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendedores")
@CrossOrigin
public class VendedorController {

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private ProductoService productoService;

    /**
     * ENDPOINT: GET /api/vendedores
     * Devuelve la lista de "Nuestras Tiendas".
     */
    @GetMapping
    public List<Vendedor> getVendedores() {
        return vendedorRepository.findAll();
    }

    /**
     * ENDPOINT: GET /api/vendedores/{id}/productos
     * Devuelve la lista de productos para UNA tienda específica.
     */
    @GetMapping("/{id}/productos")
    public List<ProductoDTO> getProductosPorVendedor(@PathVariable Long id) {
        return productoService.getProductosByVendedor(id);
    }
    
    /**
     * ENDPOINT: GET /api/vendedores/{id}
     * Devuelve los datos de UNA tienda (para la página de envío).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vendedor> getVendedorById(@PathVariable Long id) {
        return vendedorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}