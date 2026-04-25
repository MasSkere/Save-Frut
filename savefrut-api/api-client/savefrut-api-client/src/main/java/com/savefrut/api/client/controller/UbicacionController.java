package com.savefrut.api.controller;

import com.savefrut.api.entity.Distrito;
import com.savefrut.api.entity.Provincia;
import com.savefrut.api.repository.DistritoRepository;
import com.savefrut.api.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints públicos para llenar los <select> (combobox)
 * de Provincias y Distritos en el formulario de envío.
 */
@RestController
@RequestMapping("/api/ubicacion")
@CrossOrigin
public class UbicacionController {

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private DistritoRepository distritoRepository;

    @GetMapping("/provincias")
    public List<Provincia> getProvincias() {
        return provinciaRepository.findAll();
    }

    @GetMapping("/distritos/{provinciaId}")
    public List<Distrito> getDistritosPorProvincia(@PathVariable Long provinciaId) {
        return distritoRepository.findByProvinciaId(provinciaId);
    }
}