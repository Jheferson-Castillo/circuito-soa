package com.spring.boot.carro.reservas.service.client;

import com.spring.boot.carro.reservas.service.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente hacia el servicio de Usuarios (a traves del bus) para el flujo B:
 * crear reserva -> validar saldo de minutos del alumno.
 */
@Slf4j
@Component
public class UsuarioSaldoClient {

    private final RestClient busRestClient;

    public UsuarioSaldoClient(RestClient busRestClient) {
        this.busRestClient = busRestClient;
    }

    // Respuesta del endpoint de saldo de Usuarios (coincide con SaldoResponseDTO).
    public record SaldoDTO(Long idUsuario, Integer saldoMinutos) {}

    /** Devuelve el saldo de minutos disponible del usuario (0 si viene nulo). */
    public Integer obtenerSaldoMinutos(Long idUsuario) {
        try {
            SaldoDTO saldo = busRestClient.get()
                    .uri("/api/v1/usuarios/{id}/saldo", idUsuario)
                    .retrieve()
                    .body(SaldoDTO.class);
            int minutos = (saldo == null || saldo.saldoMinutos() == null) ? 0 : saldo.saldoMinutos();
            log.info("Gateway Usuarios: saldo del usuario {} = {} min", idUsuario, minutos);
            return minutos;
        } catch (Exception e) {
            throw new BusinessException("No se pudo consultar el saldo del usuario " + idUsuario
                    + " en el servicio de Usuarios (bus): " + e.getMessage());
        }
    }
}
