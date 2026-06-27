package com.spring.boot.carro.reservas.persistence.entity;

import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.enums.TipoTransmisionEnum;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "vehiculos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"placa"}, name = "uk_placa")
})
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String placa;

    @Column(length = 50, nullable = false)
    private String marca;

    @Column(length = 50, nullable = false)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false, name = "tipo_transmision")
    private TipoTransmisionEnum tipoTransmision;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EstadoVehiculosEnum estado;

    @Column(nullable = false)
    private boolean activo;
}
