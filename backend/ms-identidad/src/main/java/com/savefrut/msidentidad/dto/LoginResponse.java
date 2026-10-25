package com.savefrut.msidentidad.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private List<String> roles;
    private String rol;
    private String nombre;

    public LoginResponse(String token, Long id, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.rol = roles.isEmpty() ? null : roles.get(0).replace("ROLE_", "");
    }
}
