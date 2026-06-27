package com.spring.boot.carro.reservas.persistence.enums;

// Copia local del estado de pago. En SOA cada servicio tiene su propia copia
// de los tipos de valor compartidos. Reservas lo usa para interpretar la
// respuesta del servicio de Pagos (via PagoClienteGateway) al validar una reserva.
public enum EstadoPagoEnum {
    PAGADO,
    PENDIENTE,
    CANCELADO
}
