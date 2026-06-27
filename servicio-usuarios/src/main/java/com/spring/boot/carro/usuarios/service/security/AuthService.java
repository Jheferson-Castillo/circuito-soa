package com.spring.boot.carro.usuarios.service.security;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import com.spring.boot.carro.usuarios.persistence.repository.UsuarioRepository;
import com.spring.boot.carro.usuarios.presentation.dto.auth.LoginRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.auth.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (usuario.getPassword() == null
                || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            throw new BadCredentialsException("El usuario está inactivo");
        }

        String token = jwtService.generarToken(usuario);

        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                usuario.getRol().name(),
                usuario.getEmail(),
                usuario.getId()
        );
    }
}
