package com.spring.boot.carro.reservas.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente hacia el servicio de Usuarios (a traves del bus) para obtener los datos basicos
 * de un alumno (lo usa el instructor en "mis-alumnos").
 *
 * A diferencia de /saldo (ruta publica en el bus), /datos-basicos NO es publica, asi que
 * propagamos el token del instructor para que el bus la deje pasar.
 *
 * Degrada con gracia: si Usuarios/bus no responde, devuelve null (el llamador muestra solo el id).
 */
@Slf4j
@Component
public class UsuarioDatosClient {

    private final RestClient busRestClient;

    public UsuarioDatosClient(RestClient busRestClient) {
        this.busRestClient = busRestClient;
    }

    // Respuesta del endpoint de Usuarios (coincide con DatosBasicosUsuarioDTO de Usuarios).
    public record DatosBasicosDTO(Long id, String nombre, String apellido, String email, String telefono) {}

    public DatosBasicosDTO obtenerDatosBasicos(Long idUsuario, String bearerToken) {
        try {
            DatosBasicosDTO datos = busRestClient.get()
                    .uri("/api/v1/usuarios/{id}/datos-basicos", idUsuario)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(DatosBasicosDTO.class);
            log.info("Gateway Usuarios: datos basicos del usuario {} obtenidos", idUsuario);
            return datos;
        } catch (Exception e) {
            // Degradacion: no rompemos la lista de alumnos si Usuarios no responde.
            log.warn("mis-alumnos: no se pudieron obtener los datos del usuario {} ({})",
                    idUsuario, e.getMessage());
            return null;
        }
    }
}
