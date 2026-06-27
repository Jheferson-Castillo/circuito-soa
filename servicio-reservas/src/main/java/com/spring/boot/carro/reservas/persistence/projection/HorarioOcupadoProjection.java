package com.spring.boot.carro.reservas.persistence.projection;

import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;

import java.time.LocalDateTime;

// Se quitaron getNombre()/getApellido(): el nombre del usuario vive en el
// servicio de Usuarios y se resolveria via Camel (Fase 6), no por JPA.
public interface HorarioOcupadoProjection {
    Long getIdReserva();
    LocalDateTime getInicio();
    LocalDateTime getFin();
    Long getIdPago();
    Long getIdVehiculo();
    EstadoReservaEnum getEstado();
    String getPlacaVehiculo();
    Integer getMinutosReservados();
}
