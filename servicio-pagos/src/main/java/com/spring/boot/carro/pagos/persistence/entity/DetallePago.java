package com.spring.boot.carro.pagos.persistence.entity;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "comprobante_detalle_pago")
// 1. Indicamos que esta entidad usa la clave compuesta definida en DetallePagoId
@IdClass(DetallePagoId.class)
public class DetallePago {

    // 2. Primer componente de la clave: La relación ManyToOne (ID del Pago)
    @Id // Marca este campo como parte de la clave primaria compuesta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_pago_pago"))
    private Pago pago;

    // 3. Segundo componente de la clave: El número de cuota
    @Id // Marca este campo como la otra parte de la clave primaria compuesta
    @Column(nullable = false, name = "numero_cuota")
    private Integer numeroCuota; // El nombre 'numeroCuota' coincide con el campo en DetallePagoId

    @Column(precision = 10, scale = 2,name = "monto_cuota",nullable = false)
    private BigDecimal montoCuota;

    @Column(name = "fecha_vencimiento",nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name ="metodo_pago",nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoPagoEnum metodoPago;

    @Column(name ="estado_cuota",nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPagoEnum estadoCuota;
}
