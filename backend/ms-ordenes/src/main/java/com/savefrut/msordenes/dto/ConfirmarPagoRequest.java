package com.savefrut.msordenes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmarPagoRequest(
        @NotNull Long ordenId,
        @NotBlank String paymentId
) {}
