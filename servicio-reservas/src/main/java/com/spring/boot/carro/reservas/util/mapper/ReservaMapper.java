package com.spring.boot.carro.reservas.util.mapper;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// Ya NO usa PagoMapper: el Pago vive en otro servicio, aqui solo manejamos idPago (Long).
@Mapper(componentModel = "spring", uses = {VehiculoMapper.class})
public interface ReservaMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idPago", source = "dto.pagoId")
    @Mapping(target = "vehiculo", source = "vehiculo")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaFin", ignore = true)
    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "emailCreador", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Reserva toEntity(ReservaRequestDTO dto, Vehiculo vehiculo);

    // idPago se mapea automaticamente (mismo nombre). nombre/apellido/numeroBoleta eran remotos: se quitaron.
    @Mapping(target = "placaVehiculo", source = "vehiculo.placa")
    @Mapping(target = "modeloVehiculo", source = "vehiculo.modelo")
    ReservaResponseDTO toResponse(Reserva reserva);


    @Mapping(target = "inicio", source = "fechaReserva")
    @Mapping(target = "fin", source = "fechaFin")
    @Mapping(target = "placaVehiculo", source = "vehiculo.placa")
    @Mapping(target = "idVehiculo", source = "vehiculo.id")
    @Mapping(target = "idPago", source = "idPago")
    @Mapping(target = "idReserva", source = "id")
    HorarioOcupadoDTO toResponseHorarioOcupadoDTO(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);

    @Mapping(target = "vehiculo", source = "vehiculo", qualifiedByName = "toVehiculoResumenDTO")
    DetalleReservaResponseDTO toDetalleDTO(Reserva reserva);

}
