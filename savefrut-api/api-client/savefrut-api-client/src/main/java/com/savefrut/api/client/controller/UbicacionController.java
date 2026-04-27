package com.savefrut.api.client.controller;

import com.savefrut.api.common.dto.DistritoDTO;
import com.savefrut.api.common.dto.ProvinciaDTO;
import com.savefrut.api.common.mapper.UbicacionMapper;
import com.savefrut.api.common.repository.DistritoRepository;
import com.savefrut.api.common.repository.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ubicacion")
@RequiredArgsConstructor
public class UbicacionController {

    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;
    private final UbicacionMapper ubicacionMapper;

    @GetMapping("/provincias")
    public List<ProvinciaDTO> getProvincias() {
        return provinciaRepository.findAll().stream()
                .map(ubicacionMapper::toProvinciaDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/distritos/{provinciaId}")
    public List<DistritoDTO> getDistritosPorProvincia(@PathVariable Long provinciaId) {
        return distritoRepository.findByProvinciaId(provinciaId).stream()
                .map(ubicacionMapper::toDistritoDTO)
                .collect(Collectors.toList());
    }
}