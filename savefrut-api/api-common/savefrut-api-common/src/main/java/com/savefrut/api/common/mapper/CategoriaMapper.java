package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.CategoriaDTO;
import com.savefrut.api.common.entity.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDTO(Categoria categoria);
}