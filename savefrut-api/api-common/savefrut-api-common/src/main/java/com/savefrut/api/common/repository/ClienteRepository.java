package com.savefrut.api.repository;

import com.savefrut.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Busca un Cliente usando el ID de Usuario (que viene del token JWT)
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}