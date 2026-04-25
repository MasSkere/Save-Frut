package com.savefrut.api.repository;

import com.savefrut.api.entity.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    /**
     * Busca todos los items de detalle que pertenecen a un ID de orden específico.
     * (Este es el método que necesita el ComprobanteDTO)
     */
    List<DetalleOrden> findByOrdenId(Long ordenId);
}