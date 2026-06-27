package com.spring.boot.carro.reservas.service.scheduler;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.service.scheduler.job.ReservaFinJob;
import com.spring.boot.carro.reservas.service.scheduler.job.ReservaInicioJob;
import com.spring.boot.carro.reservas.service.scheduler.job.ReservaNotificacionJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class ReservaJobSchedulerService {

    //  Interfaz principal para programar, desprogramar y gestionar Jobs.
    @Autowired
    private  Scheduler scheduler;


    public void programarJobsReserva(Reserva reserva) {
        eliminarJobsReserva(reserva.getId()); // Siempre limpiar antes de programar

        programarInicio(reserva);
        programarFin(reserva);
        programarNotificacion(reserva, 60, "CLIENTE"); // Notificación 1h
        programarNotificacion(reserva, 10, "GMAIL_USUARIO"); // Notificación 10m
    }

    private void programarNotificacion(Reserva reserva, int minutos, String tipo) {
        LocalDateTime tiempo = reserva.getFechaReserva().minusMinutes(minutos);
        if (tiempo.isBefore(LocalDateTime.now())) return;

        JobDetail job = JobBuilder.newJob(ReservaNotificacionJob.class)
                .withIdentity("notif-" + tipo + "-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId()) // SOLO pasamos el ID
                .usingJobData("tipoNotif", tipo)
                .usingJobData("minutosAntes", minutos)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(tiempo
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        schedule(job, trigger);
    }

    public void eliminarJobsReserva(Long reservaId) {
        try {
            scheduler.deleteJob(JobKey.jobKey("inicio-reserva-" + reservaId));
            scheduler.deleteJob(JobKey.jobKey("fin-reserva-" + reservaId));
            scheduler.deleteJob(JobKey.jobKey("notif-CLIENTE-" + reservaId));
            scheduler.deleteJob(JobKey.jobKey("notif-GMAIL_USUARIO-" + reservaId));
            log.info("🧹 BD Quartz limpia para reserva ID: {}", reservaId);
        } catch (SchedulerException e) {
            log.error("Error al eliminar jobs de la reserva {}", reservaId, e);
            throw new RuntimeException("Error eliminando jobs Quartz", e);
        }
    }

    private void programarInicio(Reserva reserva) {

        // 1. CREACIÓN DEL JOB DETAIL
        JobDetail job = JobBuilder.newJob(ReservaInicioJob.class)
                .withIdentity("inicio-reserva-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId())
                .build();

        // 2. CREACIÓN DEL TRIGGER (CUÁNDO EJECUTAR)
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(reserva.getFechaReserva()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        // 3. PROGRAMACIÓN
        schedule(job, trigger);
    }

    private void programarFin(Reserva reserva) {

        // 1. CREACIÓN DEL JOB DETAIL
        JobDetail job = JobBuilder.newJob(ReservaFinJob.class)
                .withIdentity("fin-reserva-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId())
                .build();

        // 2. CREACIÓN DEL TRIGGER (CUÁNDO EJECUTAR)
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(reserva.getFechaFin()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        // 3. PROGRAMACIÓN
        schedule(job, trigger);
    }


    private void schedule(JobDetail job, Trigger trigger) {
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Error programando job Quartz", e);
        }
    }
}
