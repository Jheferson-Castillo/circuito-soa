package com.spring.boot.carro.usuarios.presentation.dto.usuario;

import com.spring.boot.carro.usuarios.persistence.enums.RolEnum;
import jakarta.validation.constraints.NotNull;

// Cuerpo del endpoint PATCH /api/v1/usuarios/{id}/rol: solo el nuevo rol a asignar.
public record CambiarRolRequestDTO(
        @NotNull(message = "El rol es obligatorio")
        RolEnum rol
) {}
