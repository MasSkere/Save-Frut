package com.savefrut.mscatalogo.mapper;

import com.savefrut.mscatalogo.dto.CategoriaDTO;
import com.savefrut.mscatalogo.entity.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDTO(Categoria categoria);
}
