package com.spring.boot.carro.reservas.presentation.controller;

import com.spring.boot.carro.reservas.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.PagoMinutosDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.ReprogramacionRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.AlumnoConClasesDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.AsignarInstructorRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaResponseDTO;
import com.spring.boot.carro.reservas.service.interfaces.IReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    @Autowired
    private IReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crearReserva(@Valid @RequestBody ReservaRequestDTO reservaRequest) {

        ReservaResponseDTO reservaCreada = reservaService.crearReserva(reservaRequest);
        return ResponseEntity.ok(reservaCreada);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> findAll() {
        return ResponseEntity.ok(reservaService.listar());
    }

    // Agenda del instructor autenticado: solo sus reservas asignadas (su id sale del token, claim "uid").
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaResponseDTO>> misReservas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservaService.misReservas(extraerUid(jwt)));
    }

    // Alumnos del instructor autenticado: sus alumnos (datos basicos) con las clases que tienen con el.
    // Se propaga el token crudo (jwt.getTokenValue()) para la llamada inter-servicio a Usuarios via bus.
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/mis-alumnos")
    public ResponseEntity<List<AlumnoConClasesDTO>> misAlumnos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservaService.misAlumnos(extraerUid(jwt), jwt.getTokenValue()));
    }

    // El instructor finaliza una clase SUYA (control de propiedad dentro del servicio).
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ReservaResponseDTO> finalizar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservaService.finalizarReserva(id, extraerUid(jwt)));
    }

    // Extrae el id de usuario del claim "uid" del JWT. Llega como número (Long/Integer).
    private Long extraerUid(Jwt jwt) {
        Object uid = jwt.getClaim("uid");
        if (uid instanceof Number numero) {
            return numero.longValue();
        }
        return Long.valueOf(String.valueOf(uid));
    }

    @GetMapping("/calendario")
    public ResponseEntity<List<HorarioOcupadoDTO>> findAllCalendario() {
        return ResponseEntity.ok(reservaService.listarCalendario());
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<DetalleReservaResponseDTO> obtenerDetalle(@PathVariable Long id){
        return ResponseEntity.ok(reservaService.detalleReserva(id));
    }

    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<ReservaResponseDTO> reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramacionRequestDTO dto) {
        ReservaResponseDTO response = reservaService.reprogramarReserva(id, dto);
        return ResponseEntity.ok(response);
    }

    // Solo el ADMIN asigna un instructor a una reserva.
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/instructor")
    public ResponseEntity<ReservaResponseDTO> asignarInstructor(@PathVariable Long id,
                                                                @Valid @RequestBody AsignarInstructorRequestDTO dto) {
        return ResponseEntity.ok(reservaService.asignarInstructor(id, dto.idInstructor()));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    // Solo el instructor asignado registra incidencias en SU reserva (control de propiedad en el servicio).
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/incidencia")
    public ResponseEntity<ReservaResponseDTO> registrarIncidencia(@PathVariable Long id,
                                                                  @RequestBody IncidenciaRequestDTO incidenciaRequestDTO,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        ReservaResponseDTO response = reservaService.registrarIncidencia(id, incidenciaRequestDTO, extraerUid(jwt));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pagoId}/minutos")
    public ResponseEntity<PagoMinutosDTO> obtenerMinutos(@PathVariable Long pagoId) {
        return ResponseEntity.ok(reservaService.detalleMinutos(pagoId));
    }

    // Incidencias de una reserva. Lo pueden llamar ADMIN/INSTRUCTOR/ALUMNO; el alcance real
    // (cuáles reservas) se valida en el servicio según el rol y el uid del token.
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','ALUMNO')")
    @GetMapping("/{id}/incidencias")
    public ResponseEntity<List<IncidenciaDTO>> verIncidencias(@PathVariable Long id,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservaService.verIncidencias(id, extraerUid(jwt), jwt.getClaimAsString("rol")));
    }

    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioOcupadoDTO>> listar(
            @RequestParam Long vehiculoId,
            @RequestParam Long pagoId) {
        return ResponseEntity.ok(reservaService.obtenerHorarios(vehiculoId, pagoId));
    }
}
