package com.spring.boot.carro.usuarios.util.mapper;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioResumenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaRegistro", ignore = true)
    // El saldo de minutos siempre arranca en 0 al crear el usuario.
    @Mapping(target = "saldoMinutos", constant = "0")
    // Rol por defecto para altas via CRUD; el password lo gestiona el login/seeder, no este DTO.
    @Mapping(target = "rol", constant = "ALUMNO")
    @Mapping(target = "password", ignore = true)
//    @Mapping(target = "fechaRegistro", expression = "java(java.time.LocalDateTime.now())")
    Usuario toEntity(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO toResponse(Usuario usuario);

    List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios);

    @Named("toUsuarioResumenDTO")
    UsuarioResumenDTO toResumenDTO(Usuario usuario);


    /**
     * Permite actualizar una entidad existente a partir de un DTO.
     * Ignora el 'id' para no intentar cambiar la clave primaria.
     *
     * @param usuarioRequestDTO Los nuevos datos.
     * @param entity            La entidad existente a modificar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    // No tocamos el saldo en una actualizacion de datos personales.
    @Mapping(target = "saldoMinutos", ignore = true)
    // Tampoco rol ni password se cambian desde el DTO de datos personales.
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UsuarioRequestDTO usuarioRequestDTO, @MappingTarget Usuario entity);
}
