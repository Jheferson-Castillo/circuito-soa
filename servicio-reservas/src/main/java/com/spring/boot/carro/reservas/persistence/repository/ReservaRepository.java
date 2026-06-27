package com.spring.boot.carro.reservas.persistence.repository;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.persistence.projection.HorarioOcupadoProjection;
import com.spring.boot.carro.reservas.presentation.dto.reporte.UsoVehiculoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByIdPago(Long idPago);

    /**
     * Cruce de horario para el MISMO pago (paquete).
     * NOTA SOA: en el monolito esto era por usuario (r.pago.usuario.id). Como el usuario
     * vive en otro servicio, aqui lo validamos por idPago. El cruce real por usuario
     * (entre varios pagos del mismo cliente) se resolveria via Camel en la Fase 6.
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.idPago = :idPago AND r.activo = true AND ( (r.fechaReserva < :fin AND r.fechaFin > :inicio) )")
    long countReservasIdPagoEnMismoHorario(Long idPago, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.id <> :reservaId AND r.idPago = :idPago AND r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio)")
    long countCrucesIdPagoExcluyendoActual(Long reservaId, Long idPago, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio)")
    long countReservasEnMismoHorario(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.id <> :reservaId AND r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio)")
    long countCrucesHorarioExcluyendoActual(Long reservaId, LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByActivoTrue();

    long countReservasByIdPago(Long idPago);

    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.vehiculo.id = :vehiculoId " +
            "AND r.id != :reservaIdExcluir " +
            "AND r.estado IN ('RESERVADO', 'EN_PROGRESO') " +
            "AND ( " +
            "    (:inicio < r.fechaFin AND :fin > r.fechaReserva) " +
            ")")
    long countCrucesVehiculo(
            @Param("reservaIdExcluir") Long reservaIdExcluir,
            @Param("vehiculoId") Long vehiculoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT r.id AS idReserva, r.fechaReserva AS inicio, r.fechaFin AS fin, r.idPago AS idPago, r.vehiculo.id AS idVehiculo, r.estado AS estado, r.vehiculo.placa AS placaVehiculo, r.minutosReservados AS minutosReservados  FROM Reserva r WHERE r.vehiculo.id = :vehiculoId AND r.activo = true AND r.estado <> 'CANCELADO' ")
    List<HorarioOcupadoProjection> findHorariosOcupadosByVehiculo(Long vehiculoId);

    @Query("SELECT r.id AS idReserva, " +
            "r.fechaReserva AS inicio, " +
            "r.fechaFin AS fin, " +
            "r.idPago AS idPago, " +
            "r.vehiculo.id AS idVehiculo, " +
            "r.estado AS estado, " +
            "r.vehiculo.placa AS placaVehiculo, " +
            "r.minutosReservados AS minutosReservados " +
            "FROM Reserva r " +
            "WHERE r.activo = true " +
            "AND r.estado IN (RESERVADO, EN_PROGRESO) " +
            "AND (" +
            "     (:vehiculoId IS NOT NULL AND r.vehiculo.id = :vehiculoId) " +
            "     OR (:idPago IS NOT NULL AND r.idPago = :idPago)" +
            ")")
    List<HorarioOcupadoProjection> findHorariosOcupados(
            @Param("vehiculoId") Long vehiculoId,
            @Param("idPago") Long idPago
    );

    @Query("SELECT r.id AS idReserva, r.fechaReserva AS inicio, r.fechaFin AS fin, r.vehiculo.id AS idVehiculo, r.idPago AS idPago, r.estado AS estado, r.vehiculo.placa AS placaVehiculo, r.minutosReservados AS minutosReservados FROM Reserva r WHERE r.idPago = :idPago AND r.activo = true AND r.estado IN ('RESERVADO','EN_PROGRESO')")
    List<HorarioOcupadoProjection> findHorariosOcupadosPorCliente(Long idPago);


    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
            "WHERE r.vehiculo.id = :vehiculoId " +
            "AND r.id <> :reservaActualId " +
            "AND r.activo = true " +
            "AND r.estado IN ('RESERVADO', 'EN_PROGRESO')")
    boolean existsOtrasReservasActivas(@Param("vehiculoId") Long vehiculoId, @Param("reservaActualId") Long reservaActualId);


    // Antes hacia JOIN FETCH al pago y al usuario; ahora idPago es escalar, solo traemos el vehiculo.
    @Query("SELECT r FROM Reserva r " +
            "JOIN FETCH r.vehiculo v " +
            "WHERE r.id = :id")
    Optional<Reserva> findByIdCompleto(@Param("id") Long id);

    //GRAFOS (uso de vehiculos): es dato propio de Reservas. Hoy ningun controller lo usa;
    //se mantiene para que el dashboard lo consuma via Camel (Fase 6).
    @Query("SELECT " +
            "v.placa, " +
            "COUNT(r), " +
            "CAST(SUM(r.minutosReservados) AS long) " +
            "FROM Reserva r JOIN r.vehiculo v " +
            "WHERE MONTH(r.fechaReserva) = :mes " +
            "AND YEAR(r.fechaReserva) = :anio " +
            "AND r.estado IN ('FINALIZADO', 'INCIDENCIA') " +
            "GROUP BY v.placa")
    List<UsoVehiculoDTO> obtenerUsoVehiculosMesActual(@Param("mes") int mes, @Param("anio") int anio);
}
