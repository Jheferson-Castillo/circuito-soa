package com.spring.boot.carro.pagos.presentation.dto.pago;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.TipoPagoEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagoListadoResponseDTO {
    private Long id;
    private String numeroBoleta;
    // Antes nombreUsuario/apellidoUsuario (venian de la relacion JPA).
    // Ahora solo el id; el nombre se resolveria via Camel preguntando a Usuarios (Fase 6).
    private Long idUsuario;
    private String nombrePaquete;
    private BigDecimal monto;
    private TipoPagoEnum tipoPago;
    private EstadoPagoEnum estado;
    private LocalDateTime fechaPago;
}
