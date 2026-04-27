package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.DistritoDTO;
import com.savefrut.api.common.dto.ProvinciaDTO;
import com.savefrut.api.common.entity.Distrito;
import com.savefrut.api.common.entity.Provincia;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {
    ProvinciaDTO toProvinciaDTO(Provincia provincia);
    DistritoDTO toDistritoDTO(Distrito distrito);
}