package com.savefrut.msordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrdenRequestDTO {
    @NotBlank
    private String direccionEnvio;

    @NotBlank
    private String distritoNombre;

    @NotBlank
    @Pattern(regexp = "DOMICILIO|RECOJO", message = "El tipo de entrega debe ser DOMICILIO o RECOJO")
    private String tipoEntrega;

    /** Coordenadas elegidas en el mapa (Google Maps), opcionales. */
    private BigDecimal latitud;

    private BigDecimal longitud;

    @NotEmpty
    @Valid
    private List<ItemDTO> items;
}
