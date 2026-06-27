package com.spring.boot.carro.usuarios.configuration.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Firma/validacion de JWT con un SECRETO COMPARTIDO (HMAC HS256).
 *
 * Es el esquema mas simple de validar por otros servicios: cada Resource Server
 * (Pagos, Reservas, bus) solo necesita el MISMO valor de `security.jwt.secret`
 * para verificar la firma, sin distribuir llaves publicas ni JWKS.
 */
@Configuration
public class JwtConfig {

    private SecretKey secretKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Emite (firma) los JWT en el login.
    @Bean
    public JwtEncoder jwtEncoder(@Value("${security.jwt.secret}") String secret) {
        ImmutableSecret<SecurityContext> jwks = new ImmutableSecret<>(secretKey(secret));
        return new NimbusJwtEncoder(jwks);
    }

    // Valida los JWT entrantes (este servicio actua tambien como Resource Server).
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(secretKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
