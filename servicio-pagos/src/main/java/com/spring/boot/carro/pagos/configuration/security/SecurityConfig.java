package com.spring.boot.carro.pagos.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servicio de Pagos como RESOURCE SERVER JWT (stateless).
 *
 * - Valida los Access Token (Bearer) que emite Usuarios, usando el MISMO secreto HS256
 *   (security.jwt.secret). Token invalido/ausente -> 401.
 * - Mapea el claim "rol" del token a una authority ROLE_<rol> (autorizacion por roles).
 * - Abiertos solo: Swagger y el endpoint interno /api/v1/pagos/{id}/info (lo consume
 *   Reservas via el bus sin token, no romper los flujos de la Fase 6).
 */
@Configuration
@EnableWebSecurity
// Habilita @PreAuthorize en los controllers (gestion de paquetes restringida a ADMIN).
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(
                            // Swagger
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            // Observabilidad: Actuator (health/prometheus) lo lee Prometheus sin token
                            "/actuator/**",
                            // Endpoint interno consumido por Reservas via el bus (sin token)
                            "/api/v1/pagos/*/info"
                    ).permitAll();
                    request.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    // Valida la firma del JWT con el secreto COMPARTIDO (mismo valor que Usuarios).
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Mapea el claim "rol" del JWT a una authority ROLE_<rol> (ej. ROLE_ADMIN).
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString("rol");
            if (rol == null || rol.isBlank()) {
                return scopes.convert(jwt);
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        });
        return converter;
    }
}
