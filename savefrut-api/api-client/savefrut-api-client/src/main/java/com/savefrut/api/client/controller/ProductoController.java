package com.savefrut.api.controller;

import com.savefrut.api.dto.ProductoDTO;
import com.savefrut.api.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
* Controlador para gestionar las peticiones públicas de productos.
* Llama a ProductoService para manejar la lógica de negocio y las transacciones.
*/
@RestController
@RequestMapping("/api/productos")
@CrossOrigin // Permite que el cliente web (HTML/JS) llame a esta API
public class ProductoController {

 // --- CAMBIO: Inyectamos el Servicio, no el Repositorio ---
 @Autowired
 private ProductoService productoService;

 /**
  * ENDPOINT: GET /api/productos
  * Devuelve la lista de "Frutas Próximas a Vencer" para la homepage.
  * Llama al SP: sp_obtener_productos_disponibles
  */
 @GetMapping
 public List<ProductoDTO> getProductosDisponibles() {
     // --- CAMBIO: Llamamos al Servicio ---
     return productoService.getProductosDisponibles();
 }

 /**
  * ENDPOINT: GET /api/productos/{id}
  * Devuelve el detalle de un solo producto.
  * Llama al SP: sp_obtener_producto_por_id
  */
 @GetMapping("/{id}")
 public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Long id) {
     
     // --- CAMBIO: Llamamos al Servicio ---
     List<ProductoDTO> resultados = productoService.getProductoById(id);

     if (!resultados.isEmpty()) {
         // Si la lista no está vacía, tomamos el primer (y único) elemento
         return ResponseEntity.ok(resultados.get(0));
     } else {
         // Si la lista está vacía, significa que no se encontró
         return ResponseEntity.notFound().build();
     }
 }

 /**
  * ENDPOINT: GET /api/productos/categoria/{id}
  * Devuelve los productos filtrados por categoría.
  * Llama al SP: sp_obtener_productos_por_categoria
  */
 @GetMapping("/categoria/{id}")
 public List<ProductoDTO> getProductosByCategoria(@PathVariable Long id) {
     // --- CAMBIO: Llamamos al Servicio ---
     return productoService.getProductosByCategoria(id);
 }
 
 /**
  * ENDPOINT: GET /api/productos/buscar?term=...
  * Devuelve una lista de productos que coinciden con el término de búsqueda.
  */
 @GetMapping("/buscar")
 public List<ProductoDTO> buscarProductos(@RequestParam("term") String termino) {
     return productoService.buscarProductosPorNombre(termino);
 }
}