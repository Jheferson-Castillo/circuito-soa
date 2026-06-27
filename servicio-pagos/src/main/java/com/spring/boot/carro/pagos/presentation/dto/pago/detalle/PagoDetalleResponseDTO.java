package com.spring.boot.carro.pagos.presentation.dto.pago.detalle;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.TipoPagoEnum;
import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteResumenDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PagoDetalleResponseDTO {
    private Long id;
    private String numeroBoleta;

    // Antes UsuarioResumenDTO (relacion JPA); ahora solo el id del usuario.
    private Long idUsuario;
    private PaqueteResumenDTO paquete;

    private MetodoPagoEnum metodoPago;
    private BigDecimal monto;
    private TipoPagoEnum tipoPago;
    private EstadoPagoEnum estado;
    private LocalDateTime fechaPago;

    private List<CuotaResponseDTO> cuotas;
}
