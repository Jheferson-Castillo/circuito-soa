package com.spring.boot.carro.pagos.util.mapper;

import com.spring.boot.carro.pagos.persistence.entity.DetallePago;
import com.spring.boot.carro.pagos.persistence.entity.Pago;
import com.spring.boot.carro.pagos.persistence.entity.Paquete;
import com.spring.boot.carro.pagos.presentation.dto.pago.PagoContadoRequestDTO;
import com.spring.boot.carro.pagos.presentation.dto.pago.PagoCuotasRequestDTO;
import com.spring.boot.carro.pagos.presentation.dto.pago.PagoListadoResponseDTO;
import com.spring.boot.carro.pagos.presentation.dto.pago.PagoResumenDTO;
import com.spring.boot.carro.pagos.presentation.dto.pago.detalle.CuotaResponseDTO;
import com.spring.boot.carro.pagos.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

// Ya NO usa UsuarioMapper: el Usuario vive en otro servicio, aqui solo manejamos idUsuario (Long).
@Mapper(componentModel = "spring", uses = {PaqueteMapper.class})
public interface PagoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", source = "dto.usuarioId")
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "numeroBoleta", ignore = true)
    @Mapping(target = "monto", source = "paquete.precioTotal")
    @Mapping(target = "tipoPago", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pago toPagoContado(PagoContadoRequestDTO dto, Paquete paquete);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", source = "dto.usuarioId")
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "numeroBoleta", ignore = true)
    @Mapping(target = "monto", source = "paquete.precioTotal")
    @Mapping(target = "tipoPago", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pago toPagoCuotas(PagoCuotasRequestDTO dto, Paquete paquete);

    // idUsuario se mapea automaticamente (mismo nombre en Pago y en el DTO).
    @Mapping(target = "nombrePaquete", source = "paquete.nombre")
    PagoListadoResponseDTO toListadoResponse(Pago pago);

    @Mapping(target = "paquete", source = "paquete", qualifiedByName = "toPaqueteResumenDTO")
    @Mapping(target = "cuotas", source = "detalles")
    PagoDetalleResponseDTO toDetalleResponse(Pago pago);

    CuotaResponseDTO toCuotaResponse(DetallePago detalle);

    List<PaqueteDTO> toResponseList(List<Paquete> paquetes);

    @Mapping(target = "paquete", source = "paquete", qualifiedByName = "toPaqueteResumenDTO")
    @Named("toPagoResumenDTO")
    PagoResumenDTO toResumenDTO(Pago pago);
}
