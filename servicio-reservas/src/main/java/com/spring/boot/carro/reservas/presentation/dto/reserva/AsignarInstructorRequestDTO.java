package com.spring.boot.carro.reservas.presentation.dto.reserva;

import jakarta.validation.constraints.NotNull;

// Cuerpo del endpoint PATCH /api/v1/reservas/{id}/instructor: el id del instructor a asignar.
public record AsignarInstructorRequestDTO(
        @NotNull(message = "El id del instructor es obligatorio")
        Long idInstructor
) {}
