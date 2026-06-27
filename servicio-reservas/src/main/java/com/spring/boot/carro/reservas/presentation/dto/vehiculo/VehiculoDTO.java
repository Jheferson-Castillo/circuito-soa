package com.spring.boot.carro.reservas.presentation.dto.vehiculo;

import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.enums.TipoTransmisionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehiculoDTO {

    private Long id;

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "[A-Z0-9-]{6,10}", message = "El formato de la placa es inválido (ej. ABC-123)")
    @Size(max = 10, message = "La placa no puede exceder los 10 caracteres")
    private String placa;

    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @Size(max = 50, message = "El modelo no puede exceder los 50 caracteres")
    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El tipo de transmisión es obligatorio")
    private TipoTransmisionEnum tipoTransmision;

    private EstadoVehiculosEnum estado;
}
