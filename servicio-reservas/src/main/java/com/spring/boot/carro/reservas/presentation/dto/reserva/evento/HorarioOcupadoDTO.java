package com.spring.boot.carro.reservas.presentation.dto.reserva.evento;

import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;

import java.time.LocalDateTime;

// Se quitaron nombre/apellido del usuario (datos remotos del servicio de Usuarios).
public record HorarioOcupadoDTO(
        Long idReserva,
        LocalDateTime inicio,
        LocalDateTime fin,
        Long idPago,
        Long idVehiculo,
        EstadoReservaEnum estado,
        String placaVehiculo,
        Integer minutosReservados
) {}
