package com.savefrut.msordenes.dto;

public record PreferenciaPagoDTO(
        Long ordenId,
        String preferenceId,
        String initPoint,
        String sandboxInitPoint
) {}
