package com.spring.boot.carro.usuarios.service.security;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Genera Access Tokens JWT (HS256) con el rol del usuario en los claims.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${security.jwt.expiration-minutes:120}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("servicio-usuarios")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())   // <-- el rol viaja en el JWT
                .claim("nombre", usuario.getNombre())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationMinutes * 60;
    }
}
