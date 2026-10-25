package com.savefrut.mspuntos.repository;

import com.savefrut.mspuntos.entity.MovimientoPuntos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Long> {

    boolean existsByOrdenId(Long ordenId);

    List<MovimientoPuntos> findTop20ByClienteIdOrderByFechaDesc(Long clienteId);
}
