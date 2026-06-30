package com.spring.boot.carro.usuarios.presentation.dto.usuario;

// Datos basicos de un usuario para llamadas inter-servicio (lo consume Reservas via el bus,
// p. ej. el instructor viendo a sus alumnos). No expone password ni rol.
public record DatosBasicosUsuarioDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono
) {}
