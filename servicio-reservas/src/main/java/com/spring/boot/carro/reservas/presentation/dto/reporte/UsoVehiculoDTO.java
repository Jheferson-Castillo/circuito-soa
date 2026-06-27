package com.spring.boot.carro.reservas.presentation.dto.reporte;

// Reporte: uso de vehiculos (dato propio de Reservas). El dashboard lo consumira via Camel (Fase 6).
public record UsoVehiculoDTO(String etiqueta, Long totalReservas, Long totalMinutos) {}
