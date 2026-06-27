package com.spring.boot.carro.reservas.presentation.dto.reserva;

import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReservaMinutosDTO {
    private Long reservaId;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFin;
    private Integer minutosReservados;
    private EstadoReservaEnum estado;
    private List<IncidenciaDTO> detalle;
}
