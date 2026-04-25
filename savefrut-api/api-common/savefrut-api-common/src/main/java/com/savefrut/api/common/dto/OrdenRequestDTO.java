package com.savefrut.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrdenRequestDTO {

    // Nuevos campos para el envío
    @NotBlank
    private String direccionEnvio;
    @NotNull
    private Long distritoId;
    @NotBlank
    private String distritoNombre;
    @NotNull
    private Double costoEnvio; // El costo que el cliente seleccionó (10 o 0)

    @NotEmpty
    private List<ItemDTO> items;
    
    @NotBlank
    private String metodoPago;
}