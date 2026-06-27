package com.spring.boot.carro.reservas.service.scheduler.job;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.persistence.repository.ReservaRepository;
import com.spring.boot.carro.reservas.util.websocket.ReservaNotificationService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@NoArgsConstructor
@Slf4j
public class ReservaNotificacionJob implements Job {

    @Autowired
    private   ReservaRepository reservaRepository;

    @Autowired
    private  ReservaNotificationService notificationService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        Long reservaId = data.getLong("reservaId");
        int minutosAntes = data.getInt("minutosAntes");

        Reserva reserva = reservaRepository.findByIdCompleto(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservaId));

        // NOTA SOA: el recordatorio por correo (que necesitaba nombre/email del usuario)
        // se delega al servicio de Usuarios via Camel en la Fase 6. Aqui solo WebSocket.
        if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {
            notificationService.notificarInicioProxima(reserva, minutosAntes);
        }
    }
}
