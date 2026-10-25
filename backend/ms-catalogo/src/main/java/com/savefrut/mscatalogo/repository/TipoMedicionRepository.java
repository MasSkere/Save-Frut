package com.savefrut.mscatalogo.repository;

import com.savefrut.mscatalogo.entity.TipoMedicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMedicionRepository extends JpaRepository<TipoMedicion, Long> {
}
