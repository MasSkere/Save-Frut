package com.savefrut.mscatalogo.repository;

import com.savefrut.mscatalogo.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c JOIN FETCH p.medicion m " +
           "WHERE p.baja = false AND p.stock > 0 AND p.fechaVencimiento >= :hoy AND p.fechaVencimiento <= :hoyPlus7 ORDER BY p.fechaVencimiento")
    List<Producto> findDisponibles(@Param("hoy") LocalDate hoy, @Param("hoyPlus7") LocalDate hoyPlus7);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c JOIN FETCH p.medicion m " +
           "WHERE p.baja = false AND p.stock > 0 AND p.fechaVencimiento >= :hoy AND p.fechaVencimiento <= :hoyPlus7 AND p.categoria.id = :categoriaId ORDER BY p.fechaVencimiento")
    List<Producto> findByCategoria(@Param("hoy") LocalDate hoy, @Param("hoyPlus7") LocalDate hoyPlus7, @Param("categoriaId") Long categoriaId);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c JOIN FETCH p.medicion m " +
           "WHERE p.id = :productoId AND p.baja = false AND p.stock > 0 AND p.fechaVencimiento >= :hoy AND p.fechaVencimiento <= :hoyPlus7")
    List<Producto> findByIdAvailable(@Param("productoId") Long productoId, @Param("hoy") LocalDate hoy, @Param("hoyPlus7") LocalDate hoyPlus7);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c JOIN FETCH p.medicion m " +
           "WHERE p.baja = false AND p.stock > 0 AND p.fechaVencimiento >= :hoy AND p.fechaVencimiento <= :hoyPlus7 AND p.vendedorId = :vendedorId ORDER BY p.fechaVencimiento")
    List<Producto> findByVendedor(@Param("hoy") LocalDate hoy, @Param("hoyPlus7") LocalDate hoyPlus7, @Param("vendedorId") Long vendedorId);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c JOIN FETCH p.medicion m " +
           "WHERE p.baja = false AND p.stock > 0 AND p.fechaVencimiento >= :hoy AND p.fechaVencimiento <= :hoyPlus7 AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) ORDER BY p.fechaVencimiento")
    List<Producto> findByNombreContaining(@Param("hoy") LocalDate hoy, @Param("hoyPlus7") LocalDate hoyPlus7, @Param("termino") String termino);

    List<Producto> findAllByVendedorId(Long vendedorId);

    long countByStockLessThan(int stock);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.baja = false AND p.fechaVencimiento < :limite")
    long countPorVencer(@Param("limite") LocalDate limite);

    @Query("SELECT p FROM Producto p WHERE p.baja = false AND (p.fechaVencimiento < :limite OR p.stock < :stock) ORDER BY p.fechaVencimiento")
    List<Producto> findAlertas(@Param("limite") LocalDate limite, @Param("stock") int stock);

    @Query(value = "SELECT p FROM Producto p WHERE p.vendedorId = :vendedorId ORDER BY p.id", countQuery = "SELECT COUNT(p) FROM Producto p WHERE p.vendedorId = :vendedorId")
    Page<Producto> findByVendedorVendedorId(@Param("vendedorId") Long vendedorId, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.id = :id AND p.vendedorId = :vendedorId")
    Optional<Producto> findByIdAndVendedorVendedorId(@Param("id") Long id, @Param("vendedorId") Long vendedorId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.vendedorId = :vendedorId AND p.stock < :stock")
    long countByVendedorVendedorIdAndStockLessThan(@Param("vendedorId") Long vendedorId, @Param("stock") int stock);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.vendedorId = :vendedorId AND p.baja = false")
    long countByVendedorVendedorIdAndBajaFalse(@Param("vendedorId") Long vendedorId);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.vendedorId = :vendedorId ORDER BY p.fechaVencimiento DESC")
    List<Producto> findTop3ByVendedorId(@Param("vendedorId") Long vendedorId, Pageable pageable);
}
