package com.savefrut.api.common.repository;

import com.savefrut.api.common.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByUsuarioId(Long usuarioId);
    long countByAprobadoTrue();
}
