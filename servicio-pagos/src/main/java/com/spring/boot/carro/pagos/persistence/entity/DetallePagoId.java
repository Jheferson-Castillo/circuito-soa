package com.spring.boot.carro.pagos.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DetallePagoId implements Serializable {
    // Debe coincidir con el nombre y tipo de la propiedad en DetallePago
    private Long pago;

    // Debe coincidir con el nombre y tipo de la propiedad en DetallePago
    private Integer numeroCuota;
}
