package com.spring.boot.carro.pagos.presentation.dto.pago;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteResumenDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagoResumenDTO {

    private Long id;
    private PaqueteResumenDTO paquete;
    // Antes UsuarioResumenDTO (relacion JPA); ahora solo el id del usuario.
    private Long idUsuario;
    private String  numeroBoleta;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private EstadoPagoEnum estado;
}
