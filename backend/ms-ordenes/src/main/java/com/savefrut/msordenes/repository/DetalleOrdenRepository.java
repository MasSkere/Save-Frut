package com.savefrut.msordenes.repository;

import com.savefrut.msordenes.entity.DetalleOrden;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenId(Long ordenId);

    List<DetalleOrden> findByOrdenIdIn(Collection<Long> ordenIds);

    @Query("SELECT d FROM DetalleOrden d JOIN FETCH d.orden o " +
           "WHERE d.vendedorId = :vendedorId ORDER BY o.fechaOrden DESC")
    List<DetalleOrden> findByVendedorId(@Param("vendedorId") Long vendedorId);

    @Query("SELECT d.productoId, d.nombreProducto, SUM(d.cantidad), SUM(d.precioUnitario * d.cantidad) " +
           "FROM DetalleOrden d WHERE d.orden.estadoOrden IN :estados " +
           "GROUP BY d.productoId, d.nombreProducto ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findTopProductosVendidos(@Param("estados") Collection<String> estados, Pageable pageable);
}
