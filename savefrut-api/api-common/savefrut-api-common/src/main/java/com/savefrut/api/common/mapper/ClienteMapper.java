package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.ClienteDTO;
import com.savefrut.api.common.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "email", source = "cliente.usuario.email")
    @Mapping(target = "nombreDistrito", source = "cliente.distrito.nombre")
    ClienteDTO toDTO(Cliente cliente);
}