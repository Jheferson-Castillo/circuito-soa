package com.spring.boot.carro.reservas.persistence.repository;

import com.spring.boot.carro.reservas.persistence.entity.EventoReserva;
import com.spring.boot.carro.reservas.persistence.enums.TipoEventoReservaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoReservaRepository extends JpaRepository<EventoReserva, Long> {

    @Query("SELECT COALESCE(SUM(c.minutosAfectados),0) FROM EventoReserva c WHERE c.idPago = :idPago")
    Integer sumMinutosAfectadosByIdPago(Long idPago);

    List<EventoReserva> findByReservaIdOrderByFechaRegistroAsc(Long reservaId);

    // Eventos de una reserva filtrados por tipo (p. ej. solo INCIDENCIA), ordenados por fecha.
    List<EventoReserva> findByReservaIdAndTipoOrderByFechaRegistroAsc(Long reservaId, TipoEventoReservaEnum tipo);

    @Query("SELECT COUNT(e) FROM EventoReserva e WHERE e.reserva.id = :reservaId AND e.tipo = 'REPROGRAMADA'")
    long countReprogramaciones(Long reservaId);
}
