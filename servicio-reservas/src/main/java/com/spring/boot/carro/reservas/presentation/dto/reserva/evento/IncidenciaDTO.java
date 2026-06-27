package com.spring.boot.carro.reservas.presentation.dto.reserva.evento;

import com.spring.boot.carro.reservas.persistence.enums.TipoEventoReservaEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IncidenciaDTO {
    private Integer minutosReservadosAntes;
    private Integer minutosUsados;
    private Integer minutosDevueltos;
    private Integer minutosAfectados;
    private String detalle;
    private LocalDateTime fechaRegistro;
    private TipoEventoReservaEnum tipo;
}
