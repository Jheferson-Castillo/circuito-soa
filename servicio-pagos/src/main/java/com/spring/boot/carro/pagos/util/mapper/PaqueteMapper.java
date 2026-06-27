package com.spring.boot.carro.pagos.util.mapper;

import com.spring.boot.carro.pagos.persistence.entity.Paquete;
import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteDTO;
import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteResumenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaqueteMapper {

    @Mapping(target = "activo", constant = "true")
    Paquete toEntity(PaqueteDTO paqueteDTO);

    PaqueteDTO toResponse(Paquete paquete);

    List<PaqueteDTO> toResponseList(List<Paquete> paquetes);

    /**
     * Permite actualizar una entidad existente a partir de un DTO.
     * Ignora el 'id' para no intentar cambiar la clave primaria.
     * @param paqueteDTO Los nuevos datos.
     * @param entity La entidad existente a modificar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo",ignore = true)
    void updateEntityFromDto(PaqueteDTO paqueteDTO, @MappingTarget Paquete entity);

    @Named("toPaqueteResumenDTO")
    PaqueteResumenDTO toResumenDTO(Paquete paquete);

}
