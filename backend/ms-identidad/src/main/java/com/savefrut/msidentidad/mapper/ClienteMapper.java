package com.savefrut.msidentidad.mapper;

import com.savefrut.msidentidad.dto.ClienteDTO;
import com.savefrut.msidentidad.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", source = "clienteId")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "nombreDistrito", source = "distrito.nombre")
    ClienteDTO toDTO(Cliente cliente);
}
