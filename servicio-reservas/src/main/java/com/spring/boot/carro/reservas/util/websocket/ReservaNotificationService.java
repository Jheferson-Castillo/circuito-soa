package com.spring.boot.carro.reservas.util.websocket;

import com.spring.boot.carro.reservas.persistence.entity.Reserva;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.ReservaNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Notificaciones de reserva por WebSocket (dato local de Reservas).
 *
 * NOTA SOA: en el monolito esto tambien enviaba correos al usuario (nombre/email),
 * usando datos del servicio de Usuarios y su EmailService. Ese envio de correo
 * se delega al servicio de Usuarios via Camel en la Fase 6; aqui solo emitimos
 * el evento por WebSocket usando datos locales de la reserva.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /** Emite por WebSocket un recordatorio de que la reserva inicia pronto. */
    public void notificarInicioProxima(Reserva reserva, int minutosAntes) {
        ReservaNotificationDTO dto = new ReservaNotificationDTO(
                reserva.getId(),
                "Tu reserva inicia en " + minutosAntes + " min",
                reserva.getFechaReserva()
        );

        // Canal por reserva (en el monolito se enrutaba por email del usuario; ahora por id de reserva).
        messagingTemplate.convertAndSend("/topic/reservas/" + reserva.getId(), dto);
        log.info("🔔 Notificacion WebSocket enviada para reserva {} ({} min antes)", reserva.getId(), minutosAntes);
    }
}
