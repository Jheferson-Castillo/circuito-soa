package com.spring.boot.carro.reservas.presentation.dto.vehiculo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VehiculoResumenDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
}
