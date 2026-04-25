package com.savefrut.api.repository;

import com.savefrut.api.entity.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DistritoRepository extends JpaRepository<Distrito, Long> {
    
    // Busca distritos por el ID de la provincia
    List<Distrito> findByProvinciaId(Long provinciaId);
}