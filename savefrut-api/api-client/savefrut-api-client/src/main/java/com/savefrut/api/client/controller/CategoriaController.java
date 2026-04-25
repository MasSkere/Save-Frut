package com.savefrut.api.controller;

import com.savefrut.api.entity.Categoria;
import com.savefrut.api.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * ENDPOINT: GET /api/categorias
     * Devuelve la lista de categorías para los botones de filtro.
     */
    @GetMapping
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }

    /**
     * ¡¡NUEVO ENDPOINT!!
     * ENDPOINT: GET /api/categorias/{id}
     * Devuelve los detalles de UNA categoría, para el título de la página.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        
        if (categoria.isPresent()) {
            return ResponseEntity.ok(categoria.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}