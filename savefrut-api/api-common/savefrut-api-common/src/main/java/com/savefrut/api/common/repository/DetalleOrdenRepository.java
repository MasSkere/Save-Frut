package com.savefrut.api.common.repository;

import com.savefrut.api.common.entity.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenId(Long ordenId);

    @Query("SELECT d FROM DetalleOrden d JOIN FETCH d.producto p WHERE p.vendedor.vendedorId = :vendedorId")
    List<DetalleOrden> findByVendedorId(@Param("vendedorId") Long vendedorId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DetalleOrden d JOIN d.producto p WHERE d.orden.id = :ordenId AND p.vendedor.vendedorId = :vendedorId")
    boolean existsByOrdenIdAndVendedorId(@Param("ordenId") Long ordenId, @Param("vendedorId") Long vendedorId);

    // Métricas dashboard vendedor
    @Query("SELECT SUM(d.precioUnitario * d.cantidad) FROM DetalleOrden d JOIN d.producto p WHERE p.vendedor.vendedorId = :vendedorId AND d.orden.fechaOrden >= :fecha")
    BigDecimal sumIngresosHoyByVendedor(@Param("vendedorId") Long vendedorId, @Param("fecha") LocalDateTime fecha);

    @Query("SELECT COUNT(DISTINCT d.orden.id) FROM DetalleOrden d JOIN d.producto p WHERE p.vendedor.vendedorId = :vendedorId AND d.orden.estadoOrden = 'Pendiente'")
    long countOrdenesPendientesByVendedor(@Param("vendedorId") Long vendedorId);

    @Query("SELECT SUM(d.precioUnitario * d.cantidad) FROM DetalleOrden d JOIN d.producto p WHERE p.vendedor.vendedorId = :vendedorId AND d.orden.fechaOrden >= :fecha")
    BigDecimal sumIngresosByVendedor(@Param("vendedorId") Long vendedorId, @Param("fecha") LocalDateTime fecha);

    @Query("SELECT COUNT(DISTINCT d.orden.id) FROM DetalleOrden d JOIN d.producto p WHERE p.vendedor.vendedorId = :vendedorId AND d.orden.fechaOrden >= :fecha")
    long countOrdenesByVendedor(@Param("vendedorId") Long vendedorId, @Param("fecha") LocalDateTime fecha);

    // Para admin dashboard - corregido
    @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.cantidad), SUM(d.precioUnitario) * SUM(d.cantidad) FROM DetalleOrden d GROUP BY d.producto.id, d.producto.nombre ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findTop5ProductosVendidos();
}