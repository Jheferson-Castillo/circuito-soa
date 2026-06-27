package com.spring.boot.carro.pagos.service.interfaces;

import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.pagos.presentation.dto.pago.*;
import com.spring.boot.carro.pagos.presentation.dto.pago.detalle.PagoDetalleResponseDTO;

import java.util.List;

public interface IPagoService {

    public List<PagoListadoResponseDTO> listarPagos();

    // Datos del pago para el servicio de Reservas (flujo cruzado via bus).
    public PagoInfoResponseDTO obtenerInfo(Long id);

    public PagoDetalleResponseDTO obtenerPagoConCuotas(Long id);

    public PagoListadoResponseDTO crearPagoContado(PagoContadoRequestDTO pagoRequestDTO);

    public PagoDetalleResponseDTO crearPagoCuotas(PagoCuotasRequestDTO pagoRequestDTO);

    public void pagarCuota(Long idPago, Integer numeroCuota, MetodoPagoEnum metodoPago);

    public void suspenderPago(Long id);

}
