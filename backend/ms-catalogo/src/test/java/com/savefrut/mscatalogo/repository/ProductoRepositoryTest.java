package com.savefrut.mscatalogo.repository;

import com.savefrut.mscatalogo.entity.Categoria;
import com.savefrut.mscatalogo.entity.Producto;
import com.savefrut.mscatalogo.entity.TipoMedicion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto crearProductoBase(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre("Frutas");
        categoria.setIconoUrl("icon.png");
        entityManager.persist(categoria);

        TipoMedicion tipoMedicion = new TipoMedicion();
        tipoMedicion.setNombre("Kg");
        entityManager.persist(tipoMedicion);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion("Producto de prueba");
        producto.setPrecioNormal(new BigDecimal("5.00"));
        producto.setStock(50);
        producto.setFechaVencimiento(LocalDate.now().plusDays(3));
        producto.setImagenUrl("https://placehold.co/600x400");
        producto.setVendedorId(1L);
        producto.setCategoria(categoria);
        producto.setMedicion(tipoMedicion);
        producto.setBaja(false);

        return producto;
    }

    @Test
    @DisplayName("CRUD 1 - Insertar (Create): Guardar producto en base de datos")
    void testCrearProducto() {
        Producto producto = crearProductoBase("Manzana Test");
        Producto guardado = productoRepository.save(producto);
        entityManager.flush();

        assertNotNull(guardado.getId());
        assertEquals("Manzana Test", guardado.getNombre());
        assertEquals(50, guardado.getStock());
        assertFalse(guardado.getBaja());
    }

    @Test
    @DisplayName("CRUD 2 - Listar (Read): Obtener todos los productos")
    void testListarProductos() {
        Producto producto1 = crearProductoBase("Manzana");
        Producto producto2 = crearProductoBase("Pera");
        productoRepository.save(producto1);
        productoRepository.save(producto2);
        entityManager.flush();

        List<Producto> productos = productoRepository.findAll();

        assertNotNull(productos);
        assertTrue(productos.size() >= 2);
    }

    @Test
    @DisplayName("CRUD 3 - Actualizar (Update): Modificar stock y precio")
    void testActualizarProducto() {
        Producto producto = crearProductoBase("Platano");
        Producto guardado = productoRepository.save(producto);
        entityManager.flush();

        guardado.setStock(30);
        guardado.setPrecioNormal(new BigDecimal("3.50"));
        Producto actualizado = productoRepository.save(guardado);
        entityManager.flush();

        Optional<Producto> encontrado = productoRepository.findById(actualizado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(30, encontrado.get().getStock());
        assertEquals(new BigDecimal("3.50"), encontrado.get().getPrecioNormal());
    }

    @Test
    @DisplayName("CRUD 4 - Eliminar/Dar de baja (Delete): Marcar producto como baja")
    void testEliminarODarDeBajaProducto() {
        Producto producto = crearProductoBase("Naranja");
        Producto guardado = productoRepository.save(producto);
        entityManager.flush();

        guardado.setBaja(true);
        productoRepository.save(guardado);
        entityManager.flush();

        Optional<Producto> encontrado = productoRepository.findById(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertTrue(encontrado.get().getBaja());
    }
}
