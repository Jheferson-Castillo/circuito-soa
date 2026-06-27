package com.spring.boot.carro.bus.security;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Filtro JWT del bus: se ejecuta ANTES de enrutar.
 *
 * Marca cada peticion en el header "busAuth":
 *   - PUBLIC   : ruta sin token (login, swagger, endpoints internos) -> se deja pasar.
 *   - OK       : token Bearer valido -> se deja pasar (se conserva la cabecera Authorization).
 *   - REJECTED : token ausente/invalido/manipulado/expirado -> se prepara un 401 y la ruta se detiene.
 */
@Component
public class JwtAuthProcessor implements Processor {

    private final JwtValidador validador;

    public JwtAuthProcessor(JwtValidador validador) {
        this.validador = validador;
    }

    @Override
    public void process(Exchange exchange) {
        Message in = exchange.getMessage();
        String path = in.getHeader("fwdPath", String.class);

        // Rutas publicas: NO requieren token.
        if (esRutaPublica(path)) {
            in.setHeader("busAuth", "PUBLIC");
            return;
        }

        String auth = in.getHeader("Authorization", String.class);
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            rechazar(in, "falta el token Bearer");
            return;
        }

        String token = auth.substring(7).trim();
        if (!validador.esValido(token)) {
            rechazar(in, "token invalido o expirado");
            return;
        }

        // Token valido: continua. La cabecera Authorization se conserva para el backend.
        in.setHeader("busAuth", "OK");
    }

    private boolean esRutaPublica(String path) {
        if (path == null) {
            return false;
        }
        // Login (emision de token)
        if (path.startsWith("/api/v1/usuarios/auth")) {
            return true;
        }
        // Swagger de los servicios
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
            return true;
        }
        // Endpoint interno de Pagos consumido por Reservas (Fase 6)
        if (path.matches("/api/v1/pagos/[^/]+/info")) {
            return true;
        }
        // Endpoints internos de saldo en Usuarios (Fase 6): /saldo y /saldo/cargar
        if (path.matches("/api/v1/usuarios/[^/]+/saldo(/.*)?")) {
            return true;
        }
        return false;
    }

    private void rechazar(Message in, String motivo) {
        in.setHeader("busAuth", "REJECTED");
        in.setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        in.setHeader(Exchange.CONTENT_TYPE, "application/json");
        in.setBody("{\"error\":\"Bus ESB: " + motivo + " (no autorizado)\"}");
    }
}
