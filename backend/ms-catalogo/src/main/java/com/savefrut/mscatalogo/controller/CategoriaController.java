package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.dto.CategoriaDTO;
import com.savefrut.mscatalogo.mapper.CategoriaMapper;
import com.savefrut.mscatalogo.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @GetMapping
    public List<CategoriaDTO> getCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> getCategoriaById(@PathVariable Long id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
