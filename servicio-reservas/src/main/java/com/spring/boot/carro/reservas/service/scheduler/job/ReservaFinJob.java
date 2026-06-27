package com.spring.boot.carro.reservas.service.scheduler.job;

import com.spring.boot.carro.reservas.persistence.entity.EventoReserva;
import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.enums.TipoEventoReservaEnum;
import com.spring.boot.carro.reservas.persistence.repository.EventoReservaRepository;
import com.spring.boot.carro.reservas.persistence.repository.ReservaRepository;
import com.spring.boot.carro.reservas.persistence.repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class ReservaFinJob implements Job {

    @Autowired
    private  ReservaRepository reservaRepository;
    @Autowired
    private  EventoReservaRepository eventoReservaRepository;
    @Autowired
    private  VehiculoRepository vehiculoRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {

        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        Reserva reserva = reservaRepository.findByIdCompleto(reservaId)
                .orElseThrow();

        if (reserva.getEstado() != EstadoReservaEnum.EN_PROGRESO) return;

        reserva.setEstado(EstadoReservaEnum.FINALIZADO);
        reserva.setActivo(false);

        long minutosUsados = Duration.between(
                reserva.getFechaReserva(),
                reserva.getFechaFin()
        ).toMinutes();

        EventoReserva evento = EventoReserva.builder()
                .idPago(reserva.getIdPago())
                .reserva(reserva)
                .minutosReservadosAntes(reserva.getMinutosReservados())
                .minutosUsados((int) minutosUsados)
                .minutosDevueltos(0)
                .minutosAfectados(0)
                .detalle("Reserva finalizada")
                .tipo(TipoEventoReservaEnum.FINALIZADO)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(evento);

        Vehiculo vehiculo = reserva.getVehiculo();

        boolean tieneMasReservas = reservaRepository.existsOtrasReservasActivas(vehiculo.getId(), reservaId);

        if (tieneMasReservas) {
            vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
            log.info("ℹ️ Vehículo ID: {} continúa RESERVADO debido a otras reservas pendientes.", vehiculo.getId());
        } else {
            vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            log.info("✅ Vehículo ID: {} ahora está DISPONIBLE.", vehiculo.getId());
        }
        vehiculoRepository.save(vehiculo);

        reservaRepository.save(reserva);
        log.info("🔥 JOB ACTIVADO: Reserva Fin - ID: {}. Estado cambiado a FINALIZADO.", reservaId);
    }
}
