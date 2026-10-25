package com.savefrut.msidentidad.mapper;

import com.savefrut.msidentidad.dto.VendedorDTO;
import com.savefrut.msidentidad.entity.Vendedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VendedorMapper {

    @Mapping(target = "id", source = "vendedorId")
    @Mapping(target = "nombreDistrito", source = "distrito.nombre")
    VendedorDTO toDTO(Vendedor vendedor);
}
