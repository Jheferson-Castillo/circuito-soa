package com.spring.boot.carro.reservas.util.mapper;

import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoDTO;
import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoResumenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiculoMapper {

    @Mapping(target = "activo", constant = "true")
    Vehiculo toEntity(VehiculoDTO vehiculoDTO);

    VehiculoDTO toResponse(Vehiculo vehiculo);

    List<VehiculoDTO> toResponseList(List<Vehiculo> vehiculos);

    @Named("toVehiculoResumenDTO")
    VehiculoResumenDTO toResumenDTO(Vehiculo vehiculo);

    /**
     * Permite actualizar una entidad existente a partir de un DTO.
     * Ignora el 'id' para no intentar cambiar la clave primaria.
     * @param vehiculoDTO Los nuevos datos.
     * @param entity La entidad existente a modificar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromDto(VehiculoDTO vehiculoDTO, @MappingTarget Vehiculo entity);
}
