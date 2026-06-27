package com.spring.boot.carro.usuarios.presentation.dto.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Peticion para sumar minutos al saldo del usuario (flujo A: comprar paquete -> cargar horas).
public record CargarSaldoRequestDTO(
        @NotNull(message = "Los minutos son obligatorios")
        @Positive(message = "Los minutos a cargar deben ser positivos")
        Integer minutos
) {}
