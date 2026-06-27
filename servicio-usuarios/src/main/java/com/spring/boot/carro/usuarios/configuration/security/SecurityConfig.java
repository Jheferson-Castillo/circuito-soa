package com.spring.boot.carro.usuarios.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad del servicio de Usuarios como Authorization Server propio (JWT HS256).
 * Reemplaza el antiguo OAuth2-con-Google.
 *
 * - Login (/api/v1/usuarios/auth/**) y endpoints internos de saldo: abiertos.
 * - El resto: requiere un Access Token JWT (Bearer) valido -> actua como Resource Server.
 * - El rol del claim "rol" se mapea a una authority ROLE_<rol>.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // API REST sin estado: cada peticion lleva su JWT.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(
                            // Login (emision de token) abierto
                            "/api/v1/usuarios/auth/**",
                            // Swagger
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            // Endpoints internos de saldo (los consumen Pagos/Reservas via el bus)
                            "/api/v1/usuarios/*/saldo",
                            "/api/v1/usuarios/*/saldo/**"
                    ).permitAll();
                    request.anyRequest().authenticated();
                })
                // Resource Server: valida el JWT entrante con el secreto compartido.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://angular-circuito-manejo.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
