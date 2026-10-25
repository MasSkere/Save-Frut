package com.savefrut.mspuntos.repository;

import com.savefrut.mspuntos.entity.PuntosCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PuntosClienteRepository extends JpaRepository<PuntosCliente, Long> {

    Optional<PuntosCliente> findByClienteId(Long clienteId);

    List<PuntosCliente> findTop10ByOrderByPuntosAcumuladosDesc();

    long countByTotalComprasGreaterThan(int compras);

    @Query("SELECT COALESCE(SUM(p.frutasSalvadas), 0) FROM PuntosCliente p")
    long sumFrutasSalvadas();
}
