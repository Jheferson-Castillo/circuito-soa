package com.spring.boot.carro.reservas.service.client;

/**
 * Puerta de salida hacia el servicio de Pagos (otra BD / otro servicio).
 * En la Fase 6 se implementara con una ruta Apache Camel que consulte por HTTP
 * al servicio de Pagos (puerto 8082). Por ahora hay una implementacion temporal
 * que indica que la integracion esta pendiente.
 */
public interface PagoClienteGateway {

    /** Devuelve los datos del pago necesarios para crear/validar una reserva. */
    PagoInfoDTO obtenerPagoInfo(Long idPago);

    /** Indica si el pago existe en el servicio de Pagos. */
    boolean existePago(Long idPago);
}
