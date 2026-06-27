package com.spring.boot.carro.reservas.service.client;

import com.spring.boot.carro.reservas.service.exception.BusinessException;
import com.spring.boot.carro.reservas.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Implementacion REAL del gateway hacia el servicio de Pagos (Fase 6).
 * Consulta GET /api/v1/pagos/{id}/info a traves del bus ESB.
 * Sustituye a la antigua implementacion temporal (PagoClienteGatewayPendiente).
 */
@Slf4j
@Service
public class PagoClienteGatewayHttp implements PagoClienteGateway {

    private final RestClient busRestClient;

    public PagoClienteGatewayHttp(RestClient busRestClient) {
        this.busRestClient = busRestClient;
    }

    @Override
    public PagoInfoDTO obtenerPagoInfo(Long idPago) {
        try {
            PagoInfoDTO info = busRestClient.get()
                    .uri("/api/v1/pagos/{id}/info", idPago)
                    .retrieve()
                    .body(PagoInfoDTO.class);

            if (info == null) {
                throw new NotFoundException("El servicio de Pagos no devolvio datos para el pago " + idPago);
            }
            log.info("Gateway Pagos: pago {} -> usuario {}, estado {}, duracionPaquete {} min",
                    info.idPago(), info.idUsuario(), info.estado(), info.duracionMinutosPaquete());
            return info;

        } catch (RestClientResponseException e) {
            // El servicio de Pagos respondio con error (ej. pago inexistente).
            throw new NotFoundException("No se encontro el pago " + idPago + " en el servicio de Pagos.");
        } catch (Exception e) {
            // Bus o servicio de Pagos no disponibles.
            throw new BusinessException("No se pudo contactar al servicio de Pagos (bus): " + e.getMessage());
        }
    }

    @Override
    public boolean existePago(Long idPago) {
        try {
            busRestClient.get()
                    .uri("/api/v1/pagos/{id}/info", idPago)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            return false;
        } catch (Exception e) {
            throw new BusinessException("No se pudo contactar al servicio de Pagos (bus): " + e.getMessage());
        }
    }
}
