package com.spring.boot.carro.reservas.service.scheduler.job;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.persistence.repository.ReservaRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@NoArgsConstructor
@Slf4j
public class ReservaInicioJob implements Job {

    @Autowired
    private  ReservaRepository reservaRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {

        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow();

        if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {
            reserva.setEstado(EstadoReservaEnum.EN_PROGRESO);
            reservaRepository.save(reserva);
            log.info("🔥 JOB ACTIVADO: Reserva Inicio - ID: {}. Estado cambiado a EN_PROGRESO.", reservaId);
        }
    }
}
