package com.spring.boot.carro.reservas.persistence.entity;

import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reservas")
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El Pago vive en el servicio de Pagos (otra BD): ya NO es una relacion JPA.
    // Guardamos solo el id; para validar/leer el pago, Reservas pregunta a Pagos via Camel (Fase 6).
    @Column(name = "id_pago", nullable = false)
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reserva_vehiculo"))
    private Vehiculo vehiculo;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(nullable = false)
    private Integer minutosReservados;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EstadoReservaEnum estado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoReserva> eventos = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "email_creador", length = 150)
    private String emailCreador;
}
