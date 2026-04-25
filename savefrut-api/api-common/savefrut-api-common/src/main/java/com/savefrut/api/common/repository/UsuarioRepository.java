package com.savefrut.api.repository;

import com.savefrut.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * @param email El email del usuario a buscar.
     * @return Un Optional que contiene al Usuario si se encuentra.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con ese email.
     * @param email El email a comprobar.
     *@return true si existe, false si no.
     */
    Boolean existsByEmail(String email);
    
}
    