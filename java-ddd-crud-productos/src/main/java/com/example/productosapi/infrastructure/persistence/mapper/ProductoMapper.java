package com.example.productosapi.infrastructure.persistence.mapper;

import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.infrastructure.persistence.entity.ProductoEntity;
import com.example.productosapi.infrastructure.rest.dto.ProductoRequest;
import com.example.productosapi.infrastructure.rest.dto.ProductoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mapper para convertir entre las diferentes representaciones de Producto
 */
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);

    /**
     * Convierte de ProductoEntity a Producto (dominio)
     */
    Producto toDomain(ProductoEntity entity);

    /**
     * Convierte de Producto (dominio) a ProductoEntity
     */
    ProductoEntity toEntity(Producto domain);

    /**
     * Convierte de ProductoRequest a Producto (dominio)
     */
    @Mapping(target = "id", expression = "java(generarId())")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaCreacion", expression = "java(obtenerFechaActual())")
    @Mapping(target = "fechaActualizacion", expression = "java(obtenerFechaActual())")
    Producto toDomain(ProductoRequest request);

    /**
     * Convierte de Producto (dominio) a ProductoResponse
     */
    ProductoResponse toResponse(Producto domain);

    /**
     * Convierte una lista de Productos (dominio) a lista de ProductoResponse
     */
    List<ProductoResponse> toResponseList(List<Producto> domainList);

    /**
     * Genera un UUID para nuevos productos
     */
    @Named("generarId")
    default UUID generarId() {
        return UUID.randomUUID();
    }

    /**
     * Obtiene la fecha actual para registros nuevos
     */
    @Named("obtenerFechaActual")
    default LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }
}