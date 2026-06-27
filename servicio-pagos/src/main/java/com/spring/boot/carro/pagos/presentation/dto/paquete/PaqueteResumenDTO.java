package com.spring.boot.carro.pagos.presentation.dto.paquete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PaqueteResumenDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private BigDecimal precioTotal;
}
