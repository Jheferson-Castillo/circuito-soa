package com.spring.boot.carro.reservas.presentation.dto.reserva;


import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDTO {
    private Long id;
    // Antes numeroBoleta + nombre/apellido del usuario (datos remotos del servicio de Pagos/Usuarios).
    // Ahora solo el idPago; el enriquecimiento con esos datos se hara via Camel (Fase 6).
    private Long idPago;
    // Instructor asignado a la reserva (null si aún no tiene). Lo asigna el ADMIN.
    private Long idInstructor;
    private String placaVehiculo;
    private String modeloVehiculo;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFin;
    private Integer minutosReservados;
    private EstadoReservaEnum estado;
}
