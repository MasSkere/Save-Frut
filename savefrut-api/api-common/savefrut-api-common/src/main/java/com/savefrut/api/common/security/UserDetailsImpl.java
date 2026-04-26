package com.savefrut.api.common.security;

import com.savefrut.api.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Esta clase es la que Spring Security usará como el "principal" o usuario autenticado.
 * Adapta tu clase Usuario (de la BD) al formato que Spring Security entiende.
 */
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Usuario usuario;

    public UserDetailsImpl(Usuario usuario) {
        this.usuario = usuario;
    }

    // Método clave: Obtiene el Rol desde la entidad Usuario
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Obtenemos el rol del usuario y lo convertimos a una autoridad de Spring
        return Collections.singleton(new SimpleGrantedAuthority(usuario.getRol().getNombre()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword(); // Devuelve el password_hash
    }

    @Override
    public String getUsername() {
        return usuario.getEmail(); // IMPORTANTE: El "username" para Spring será nuestro "email"
    }

    // Métodos para control de cuenta (los dejamos en true)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Podríamos usar un campo "estado_id" aquí si quisiéramos
    }
    
    // Método extra para obtener nuestro objeto Usuario completo si lo necesitamos
    public Usuario getUsuario() {
        return usuario;
    }
    
    // Método extra para obtener el ID directamente
    public Long getId() {
        return usuario.getId();
    }
}