package com.example.productosapi.infrastructure.persistence.mapper;

import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.infrastructure.persistence.entity.ClienteEntity;
import com.example.productosapi.infrastructure.rest.dto.ClienteRequest;
import com.example.productosapi.infrastructure.rest.dto.ClienteResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
/**
 *
 * @author kcabezas
 */
@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteMapper INSTANCE = Mappers.getMapper(ClienteMapper.class);

    /**
     * Convierte de ClienteEntity a Cliente (dominio)
     */
    Cliente toDomain(ClienteEntity entity);

    /**
     * Convierte de Cliente (dominio) a ClienteEntity
     */
    ClienteEntity toEntity(Cliente domain);

    /**
     * Convierte de ClienteRequest a Cliente (dominio)
     */
    @Mapping(target = "id", expression = "java(generarId())")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaCreacion", expression = "java(obtenerFechaActual())")
    @Mapping(target = "fechaActualizacion", expression = "java(obtenerFechaActual())")
    Cliente toDomain(ClienteRequest request);

    /**
     * Convierte de Cliente (dominio) a ClienteResponse
     */
    ClienteResponse toResponse(Cliente domain);

    /**
     * Convierte una lista de Clientes a lista de ClienteResponse
     */
    List<ClienteResponse> toResponseList(List<Cliente> domainList);

    @Named("generarId")
    default UUID generarId() {
        return UUID.randomUUID();
    }

    @Named("obtenerFechaActual")
    default LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }
}
