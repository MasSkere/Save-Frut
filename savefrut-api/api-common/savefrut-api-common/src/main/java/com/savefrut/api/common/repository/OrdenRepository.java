package com.savefrut.api.common.repository;

import com.savefrut.api.common.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @Query("SELECT COUNT(o) FROM Orden o WHERE o.cliente.clienteId = :clienteId")
    long countByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT o FROM Orden o WHERE o.estadoOrden = :estado")
    List<Orden> findByEstado(@Param("estado") String estado);

    @Query("SELECT COUNT(o) FROM Orden o WHERE o.estadoOrden = :estado")
    long countByEstado(@Param("estado") String estado);

    @Query("SELECT COUNT(o) FROM Orden o WHERE o.fechaOrden >= :fecha AND o.estadoOrden = :estado")
    long countByFechaAndEstado(@Param("fecha") LocalDate fecha, @Param("estado") String estado);

    @Query("SELECT SUM(o.total) FROM Orden o WHERE o.fechaOrden >= :fecha")
    BigDecimal sumTotalByFecha(@Param("fecha") LocalDate fecha);
}