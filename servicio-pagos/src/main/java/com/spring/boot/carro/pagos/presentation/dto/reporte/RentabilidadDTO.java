package com.spring.boot.carro.pagos.presentation.dto.reporte;

// Reporte 1: Paquetes
public record RentabilidadDTO(String etiqueta, Double montoTotal, Double montoPendiente, Double montoCancelado, Long totalClientes) {}
