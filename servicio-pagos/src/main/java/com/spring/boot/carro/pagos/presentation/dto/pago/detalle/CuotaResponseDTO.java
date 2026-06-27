package com.spring.boot.carro.pagos.presentation.dto.pago.detalle;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CuotaResponseDTO {
    private Integer numeroCuota;
    private BigDecimal montoCuota;
    private LocalDate fechaVencimiento;
    private EstadoPagoEnum estadoCuota;
    private MetodoPagoEnum metodoPago;
}
