package com.spring.boot.carro.usuarios.persistence.entity;

import com.spring.boot.carro.usuarios.persistence.enums.RolEnum;
import com.spring.boot.carro.usuarios.persistence.enums.TipoDocumentoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clientes", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"email"}, name = "uk_email"),
        @UniqueConstraint(columnNames = {"tipo_documento", "numero_documento"}, name = "uk_documento"),
})
@Getter
@Setter
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "tipo_documento")
    private TipoDocumentoEnum tipoDocumento;

    @Column(nullable = false, length = 20, name = "numero_documento")
    private String numeroDocumento;

   @Column(length = 20, nullable = false)
    private String telefono;

    @Column(length = 100, nullable = false)
    private String email;

    // Contrasena (hash BCrypt). Nullable: los usuarios creados por el CRUD aun no tienen
    // credenciales; las pone el seeder o un futuro endpoint de alta con credenciales.
    @Column(length = 100)
    private String password;

    // Un rol por usuario. Se incluye en los claims del JWT.
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RolEnum rol;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean activo;

    // Saldo de minutos de clase disponibles del alumno.
    // Lo usan los flujos cruzados por Camel (compra -> cargar horas, reserva -> validar saldo).
    @Builder.Default
    @Column(name = "saldo_minutos", nullable = false)
    private Integer saldoMinutos = 0;

}
