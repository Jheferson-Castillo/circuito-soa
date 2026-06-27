package com.spring.boot.carro.reservas.presentation.dto.reserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaRequestDTO {
    @NotNull
    private Long pagoId;
    @NotNull
    private Long vehiculoId;
    @NotNull
    private LocalDateTime fechaReserva;
    @NotNull
    @Positive
    private Integer minutosReservados;
}
