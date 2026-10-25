package com.savefrut.msidentidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
        @NotBlank @Pattern(regexp = "CLIENTE|VENDEDOR", message = "El rol debe ser CLIENTE o VENDEDOR") String rol,
        String nombres,
        String apellidos,
        String telefono,
        String ruc,
        String razonSocial,
        String direccion,
        Long distritoId
) {}
