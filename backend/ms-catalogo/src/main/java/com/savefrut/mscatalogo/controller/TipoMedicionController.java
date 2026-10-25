package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.entity.TipoMedicion;
import com.savefrut.mscatalogo.repository.TipoMedicionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipo-medicion")
@RequiredArgsConstructor
public class TipoMedicionController {

    private final TipoMedicionRepository tipoMedicionRepository;

    @GetMapping
    public List<TipoMedicion> listar() {
        return tipoMedicionRepository.findAll();
    }
}
