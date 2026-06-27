package com.spring.boot.carro.pagos.presentation.dto.pago;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;

/**
 * Datos del pago que necesita el servicio de Reservas (los pide via el bus).
 * Incluye la duracion del paquete para calcular saldos sin exponer la entidad completa.
 */
public record PagoInfoResponseDTO(
        Long idPago,
        Long idUsuario,
        EstadoPagoEnum estado,
        Integer duracionMinutosPaquete
) {}
