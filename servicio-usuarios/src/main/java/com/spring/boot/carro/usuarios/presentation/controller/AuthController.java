package com.spring.boot.carro.usuarios.presentation.controller;

import com.spring.boot.carro.usuarios.presentation.dto.auth.LoginRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.auth.LoginResponseDTO;
import com.spring.boot.carro.usuarios.service.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
