package com.spring.boot.carro.reservas.presentation.dto.reserva.evento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReprogramacionRequestDTO {

    @NotNull
    private Long vehiculoId;

    @NotNull(message = "La nueva fecha y hora es obligatoria")
    @Future(message = "La nueva fecha debe ser posterior a la hora actual")
    private LocalDateTime nuevaFecha;

    @NotNull
    @Positive
    private Integer minutosReservados;
}
