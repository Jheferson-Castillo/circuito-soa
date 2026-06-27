package com.spring.boot.carro.reservas.presentation.dto.reserva.evento;

import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaMinutosDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PagoMinutosDTO {
    private Long pagoId;
    private Integer minutosTotalesPaquete;
    private Integer minutosConsumidos;   // sum(minutosAfectados)
    private Integer minutosDisponibles;  // minutosTotales - minutosConsumidos
    private List<ReservaMinutosDTO> reservas;
}
