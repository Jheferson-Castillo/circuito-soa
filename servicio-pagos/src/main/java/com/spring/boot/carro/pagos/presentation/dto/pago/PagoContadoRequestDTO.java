package com.spring.boot.carro.pagos.presentation.dto.pago;

import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import jakarta.validation.constraints.NotNull;

public record PagoContadoRequestDTO(
        @NotNull(message = "El id del paquete es obligatorio") Long paqueteId,
        @NotNull(message = "El id del usuario es obligatorio") Long usuarioId,
        @NotNull(message = "El método de pago es obligatorio") MetodoPagoEnum metodoPago
) {}
