package com.spring.boot.carro.reservas.presentation.dto.reserva;

import java.util.List;

/**
 * Un alumno del instructor: sus datos basicos (traidos de Usuarios via el bus) + las clases/reservas
 * que tiene con ese instructor. Los datos personales pueden venir null si Usuarios no respondio
 * (degradacion con gracia); en ese caso al menos se conserva el idUsuario y las clases.
 */
public record AlumnoConClasesDTO(
        Long idUsuario,
        String nombre,
        String apellido,
        String email,
        String telefono,
        List<ReservaResponseDTO> clases
) {}
