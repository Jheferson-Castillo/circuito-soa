package com.spring.boot.carro.pagos.persistence.entity;

import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.TipoPagoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "comprobante_pago", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numeroBoleta"}, name = "uk_numeroBoleta")
})
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paquete", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pago_paquete"))
    private Paquete paquete;

    // El Usuario vive en el servicio de Usuarios (otra BD): ya NO es una relacion JPA.
    // Guardamos solo el id; para datos del usuario, Pagos preguntara a Usuarios via Camel (Fase 6).
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(nullable = false, name = "numero_boleta")
    private String numeroBoleta;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(length = 15,nullable = false,name = "metodo_pago")
    private MetodoPagoEnum metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(length = 15,nullable = false,name = "tipo_pago")
    private TipoPagoEnum tipoPago;

    @Column(nullable = false,name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(length = 15,nullable = false)
    private EstadoPagoEnum estado;

    @Builder.Default
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<DetallePago> detalles = new ArrayList<>();
}
