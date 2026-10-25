package com.savefrut.msordenes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDTO {
    @NotNull
    private Long productoId;

    @Min(1)
    private int cantidad;
}
