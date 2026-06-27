package com.spring.boot.carro.pagos.presentation.dto.pago.detalle;

import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;

public record PagarCuotaRequest(
        MetodoPagoEnum metodoPago) {
}
