package com.spring.boot.carro.reservas.presentation.dto.reserva;

import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoResumenDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DetalleReservaResponseDTO {
    private Long id;
    // Antes PagoResumenDTO (datos remotos). Ahora solo el idPago.
    private Long idPago;
    private VehiculoResumenDTO vehiculo;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFin;
    private EstadoReservaEnum estado;

}
