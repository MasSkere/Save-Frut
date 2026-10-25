package com.savefrut.msordenes.repository;

import com.savefrut.msordenes.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByClienteIdOrderByFechaOrdenDesc(Long clienteId);

    List<Orden> findAllByOrderByFechaOrdenDesc();

    List<Orden> findByEstadoOrdenOrderByFechaOrdenDesc(String estadoOrden);

    List<Orden> findByEstadoOrdenAndMpPreferenceIdIsNotNullAndFechaOrdenAfter(String estadoOrden, LocalDateTime desde);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Orden o " +
           "WHERE o.estadoOrden IN :estados AND o.fechaOrden >= :desde AND o.fechaOrden < :hasta")
    BigDecimal sumTotalEntre(@Param("estados") Collection<String> estados,
                             @Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(o) FROM Orden o " +
           "WHERE o.estadoOrden IN :estados AND o.fechaOrden >= :desde AND o.fechaOrden < :hasta")
    long countEntre(@Param("estados") Collection<String> estados,
                    @Param("desde") LocalDateTime desde,
                    @Param("hasta") LocalDateTime hasta);

    @Query("SELECT o.estadoOrden, COUNT(o) FROM Orden o GROUP BY o.estadoOrden")
    List<Object[]> contarPorEstado();

    /** Transición atómica PENDIENTE_PAGO → PAGADO; devuelve 0 si otra petición ya la procesó. */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Orden o SET o.estadoOrden = 'PAGADO', o.mpPaymentId = :paymentId, o.fechaPago = :fechaPago " +
           "WHERE o.id = :ordenId AND o.estadoOrden = 'PENDIENTE_PAGO'")
    int marcarPagada(@Param("ordenId") Long ordenId,
                     @Param("paymentId") String paymentId,
                     @Param("fechaPago") LocalDateTime fechaPago);
}
