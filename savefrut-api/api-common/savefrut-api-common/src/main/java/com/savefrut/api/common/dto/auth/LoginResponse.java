package com.savefrut.api.common.dto.auth;

import lombok.Data;
import java.util.List;

/**
 * DTO (Data Transfer Object) para enviar la respuesta de login exitoso.
 * Esto es el JSON que enviamos de vuelta al cliente.
 */
@Data
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private List<String> roles;

    public LoginResponse(String token, Long id, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
