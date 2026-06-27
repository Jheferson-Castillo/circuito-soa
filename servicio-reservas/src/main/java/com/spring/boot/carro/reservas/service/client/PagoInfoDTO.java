package com.spring.boot.carro.reservas.service.client;

import com.spring.boot.carro.reservas.persistence.enums.EstadoPagoEnum;

/**
 * Datos del Pago que el servicio de Reservas necesita para operar.
 * Es el contrato de respuesta del servicio de Pagos; lo resolvera el bus Camel (Fase 6).
 */
public record PagoInfoDTO(
        Long idPago,
        Long idUsuario,
        EstadoPagoEnum estado,
        Integer duracionMinutosPaquete
) {}
