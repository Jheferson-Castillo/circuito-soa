package com.spring.boot.carro.pagos.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente hacia el servicio de Usuarios (a traves del bus) para el flujo A:
 * comprar paquete -> cargar horas al saldo del alumno.
 */
@Slf4j
@Component
public class UsuarioSaldoClient {

    private final RestClient busRestClient;

    public UsuarioSaldoClient(RestClient busRestClient) {
        this.busRestClient = busRestClient;
    }

    // Cuerpo de la peticion de carga de saldo (coincide con CargarSaldoRequestDTO de Usuarios).
    public record CargarSaldoRequest(Integer minutos) {}

    /**
     * Suma minutos al saldo del usuario. Best-effort: si falla (Usuarios/bus caido),
     * se registra y NO se revierte el pago; en un sistema real seria un mensaje con reintentos.
     */
    public void cargarHoras(Long idUsuario, Integer minutos) {
        try {
            busRestClient.post()
                    .uri("/api/v1/usuarios/{id}/saldo/cargar", idUsuario)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CargarSaldoRequest(minutos))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Flujo A: cargadas {} min al saldo del usuario {}", minutos, idUsuario);
        } catch (Exception e) {
            log.warn("Flujo A: no se pudo cargar horas al usuario {} ({} min). Causa: {}",
                    idUsuario, minutos, e.getMessage());
        }
    }
}
