package com.savefrut.api.repository;

import com.savefrut.api.dto.ProductoDTO;
import com.savefrut.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Llama a la consulta nombrada "Producto.findProductosDisponibles"
     * (que definimos en Producto.java)
     */
    List<ProductoDTO> findProductosDisponibles(@Param("hoy") LocalDate hoy);

    /**
     * Llama a la consulta nombrada "Producto.findProductosDisponiblesByCategoria"
     */
    List<ProductoDTO> findProductosDisponiblesByCategoria(@Param("hoy") LocalDate hoy, 
                                                          @Param("categoriaId") Long categoriaId);

    /**
     * Llama a la consulta nombrada "Producto.findProductoDTOById"
     * Devuelve una lista (la manejamos en el controller)
     */
    List<ProductoDTO> findProductoDTOById(@Param("hoy") LocalDate hoy, 
                                          @Param("productoId") Long productoId);
    
 // +++++ +++++
    /**
     * Llama a la consulta nombrada "Producto.findProductosDisponiblesByVendedor"
     */
    List<ProductoDTO> findProductosDisponiblesByVendedor(@Param("hoy") LocalDate hoy,
                                                         @Param("vendedorId") Long vendedorId);

    /**
     * Llama al SP "sp_buscar_productos_por_nombre"
     */
    List<ProductoDTO> buscarProductosPorNombre(@Param("hoy") LocalDate hoy,
                                               @Param("termino") String termino);
}