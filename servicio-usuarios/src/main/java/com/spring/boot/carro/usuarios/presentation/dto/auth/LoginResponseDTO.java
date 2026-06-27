package com.spring.boot.carro.usuarios.presentation.dto.auth;

// Respuesta del login: el Access Token JWT y datos basicos del usuario autenticado.
public record LoginResponseDTO(
        String accessToken,
        String tokenType,        // "Bearer"
        long expiresInSeconds,
        String rol,
        String email,
        Long idUsuario
) {}
