package com.spring.boot.carro.pagos.presentation.dto.pago;

import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagoCuotasRequestDTO(
        @NotNull(message = "El id del paquete es obligatorio") Long paqueteId,
        @NotNull(message = "El id del usuario es obligatorio") Long usuarioId,
        @NotNull(message = "El método de pago es obligatorio") MetodoPagoEnum metodoPago,

        @NotNull @Positive Integer cuotas,
        @NotNull @Positive BigDecimal montoPrimerPago
) {}
