package com.savefrut.api.common.repository;

import com.savefrut.api.common.entity.TipoMedicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMedicionRepository extends JpaRepository<TipoMedicion, Long> {
}