package com.savefrut.msidentidad.controller;

import com.savefrut.msidentidad.dto.UbicacionDTO;
import com.savefrut.msidentidad.repository.DistritoRepository;
import com.savefrut.msidentidad.repository.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ubicacion")
@RequiredArgsConstructor
public class UbicacionController {

    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;

    @GetMapping("/provincias")
    public List<UbicacionDTO> getProvincias() {
        return provinciaRepository.findAll().stream()
                .map(p -> new UbicacionDTO(p.getProvinciaId(), p.getNombre()))
                .toList();
    }

    @GetMapping("/distritos/{provinciaId}")
    public List<UbicacionDTO> getDistritosPorProvincia(@PathVariable Long provinciaId) {
        return distritoRepository.findByProvinciaId(provinciaId).stream()
                .map(d -> new UbicacionDTO(d.getDistritoId(), d.getNombre()))
                .toList();
    }
}
