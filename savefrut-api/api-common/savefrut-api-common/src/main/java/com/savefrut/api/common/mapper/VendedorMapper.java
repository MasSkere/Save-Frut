package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.VendedorDTO;
import com.savefrut.api.common.entity.Vendedor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendedorMapper {
    VendedorDTO toDTO(Vendedor vendedor);
}