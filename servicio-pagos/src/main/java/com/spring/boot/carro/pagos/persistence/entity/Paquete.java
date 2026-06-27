package com.spring.boot.carro.pagos.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "paquete_planes")
@Builder
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(nullable = false)
    private boolean activo;
}
