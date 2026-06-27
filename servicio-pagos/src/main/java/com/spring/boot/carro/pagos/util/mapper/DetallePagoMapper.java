package com.spring.boot.carro.pagos.util.mapper;


import com.spring.boot.carro.pagos.persistence.entity.DetallePago;
import com.spring.boot.carro.pagos.presentation.dto.pago.detalle.CuotaResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetallePagoMapper {

    CuotaResponseDTO toDto(DetallePago detalle);
    List<CuotaResponseDTO> toDtoList(List<DetallePago> detalles);
}
