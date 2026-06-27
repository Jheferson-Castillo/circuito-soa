package com.spring.boot.carro.usuarios.presentation.dto.usuario;

// Respuesta con el saldo de minutos del usuario (lo consume Reservas via el bus en el flujo B).
public record SaldoResponseDTO(Long idUsuario, Integer saldoMinutos) {}
