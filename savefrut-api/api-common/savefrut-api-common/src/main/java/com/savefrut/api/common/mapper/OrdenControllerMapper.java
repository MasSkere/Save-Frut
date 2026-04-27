package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.OrdenDTO;
import com.savefrut.api.common.entity.Orden;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdenControllerMapper {
    OrdenDTO toDTO(Orden orden);
}