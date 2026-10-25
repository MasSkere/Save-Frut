package com.savefrut.msordenes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteResumenDTO(
        Long id,
        String nombres,
        String apellidos,
        String email
) {
    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
