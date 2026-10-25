package com.savefrut.msordenes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VendedorResumenDTO(
        Long id,
        String razonSocial
) {}
