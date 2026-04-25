package com.savefrut.api.entity;

//--- Importaciones para el Mapeo del SP ---
import com.savefrut.api.dto.ProductoDTO;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.NamedNativeQuery; // <-- Importante
//--- Fin importaciones ---

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//ESTA ES LA SOLUCIÓN DEFINITIVA PARA EL MAPEO DEL SP
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//1. Definimos el "mapa" (SqlResultSetMapping) que traduce el SP al DTO
@SqlResultSetMapping(
 name = "ProductoDTOMapping", 
 classes = @ConstructorResult(
     targetClass = ProductoDTO.class, // La clase DTO de destino
     columns = {
         // Los nombres DEBEN coincidir con los alias del SP
         @ColumnResult(name = "id", type = Long.class),
         @ColumnResult(name = "nombre", type = String.class),
         @ColumnResult(name = "descripcion", type = String.class),
         @ColumnResult(name = "precioNormal", type = BigDecimal.class),
         @ColumnResult(name = "precioDescuento", type = BigDecimal.class),
         @ColumnResult(name = "fechaVencimiento", type = LocalDate.class),
         @ColumnResult(name = "diasParaVencer", type = Integer.class),
         @ColumnResult(name = "imagenUrl", type = String.class),
         @ColumnResult(name = "nombreMedicion", type = String.class),
         @ColumnResult(name = "nombreCategoria", type = String.class),
         @ColumnResult(name = "nombreVendedor", type = String.class),
         @ColumnResult(name = "vendedorId", type = Long.class)
     }
 )
)
//2. Definimos "consultas nativas con nombre" que USAN ese mapa
@NamedNativeQuery(
 name = "Producto.findProductosDisponibles", // Nombre de esta consulta
 query = "EXEC sp_obtener_productos_disponibles :hoy", // El SP que llama
 resultSetMapping = "ProductoDTOMapping" // El mapa que usa
)
@NamedNativeQuery(
 name = "Producto.findProductosDisponiblesByCategoria",
 query = "EXEC sp_obtener_productos_por_categoria :hoy, :categoriaId",
 resultSetMapping = "ProductoDTOMapping"
)
@NamedNativeQuery(
 name = "Producto.findProductoDTOById",
 query = "EXEC sp_obtener_producto_por_id :hoy, :productoId",
 resultSetMapping = "ProductoDTOMapping"
)
@NamedNativeQuery(
name = "Producto.findProductosDisponiblesByVendedor",
query = "EXEC sp_obtener_productos_por_vendedor :hoy, :vendedorId",
resultSetMapping = "ProductoDTOMapping"
	)

@NamedNativeQuery(
name = "Producto.buscarProductosPorNombre",
query = "EXEC sp_buscar_productos_por_nombre :hoy, :termino",
resultSetMapping = "ProductoDTOMapping"
	)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

@Data
@Entity
@Table(name = "productos")
public class Producto {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "producto_id")
 private Long id;

 @Column(name = "nombre", nullable = false, length = 100)
 private String nombre;

 @Column(name = "descripcion", length = 500)
 private String descripcion;

 @Column(name = "precio_normal", nullable = false)
 private BigDecimal precioNormal;

 @Column(name = "stock", nullable = false)
 private Integer stock;

 @Column(name = "fecha_vencimiento", nullable = false)
 private LocalDate fechaVencimiento;

 @Column(name = "imagen_url", length = 255, nullable = true)
 private String imagenUrl;

 @Column(name = "fecha_publicacion", updatable = false)
 private LocalDateTime fechaPublicacion;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "vendedor_id", nullable = false)
 private Vendedor vendedor;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "categoria_id", nullable = false)
 private Categoria categoria;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "medicion_id", nullable = false)
 private TipoMedicion medicion;

 @PrePersist
 protected void onCreate() {
     this.fechaPublicacion = LocalDateTime.now();
 }
}