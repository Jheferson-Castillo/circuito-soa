package com.spring.boot.carro.bus.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Valida un Access Token JWT (HS256) con el secreto COMPARTIDO.
 * Usa la libreria JOSE de Nimbus directamente (sin Spring Security) para que el
 * bus siga siendo un proxy Camel "plano" y no se auto-bloquee con una cadena de
 * seguridad de Spring.
 */
@Component
public class JwtValidador {

    private final byte[] secret;

    public JwtValidador(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** true solo si la firma HS256 es correcta y el token no ha expirado. */
    public boolean esValido(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            // Debe estar firmado con HS256 (el algoritmo que emite Usuarios).
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                return false;
            }
            // Firma valida con el secreto compartido.
            if (!jwt.verify(new MACVerifier(secret))) {
                return false;
            }
            // No expirado.
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            return exp != null && exp.after(new Date());

        } catch (Exception e) {
            // Token mal formado, manipulado, etc.
            return false;
        }
    }
}
