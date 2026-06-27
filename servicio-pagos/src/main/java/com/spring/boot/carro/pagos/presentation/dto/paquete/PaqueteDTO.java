package com.spring.boot.carro.pagos.presentation.dto.paquete;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaqueteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción es demasiado larga")
    private String descripcion;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Min(value = 30, message = "La duración debe ser al menos 30 minutos")
    private Integer duracionMinutos;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener hasta 2 decimales")
    private BigDecimal precioTotal;

}
