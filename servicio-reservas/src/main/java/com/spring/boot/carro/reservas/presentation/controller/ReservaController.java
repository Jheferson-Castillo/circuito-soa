package com.spring.boot.carro.reservas.presentation.controller;

import com.spring.boot.carro.reservas.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.PagoMinutosDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.ReprogramacionRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaResponseDTO;
import com.spring.boot.carro.reservas.service.interfaces.IReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/incidencia")
    public ResponseEntity<ReservaResponseDTO> registrarIncidencia(@PathVariable Long id, @RequestBody IncidenciaRequestDTO incidenciaRequestDTO) {
        ReservaResponseDTO response = reservaService.registrarIncidencia(id, incidenciaRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pagoId}/minutos")
    public ResponseEntity<PagoMinutosDTO> obtenerMinutos(@PathVariable Long pagoId) {
        return ResponseEntity.ok(reservaService.detalleMinutos(pagoId));
    }

    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioOcupadoDTO>> listar(
            @RequestParam Long vehiculoId,
            @RequestParam Long pagoId) {
        return ResponseEntity.ok(reservaService.obtenerHorarios(vehiculoId, pagoId));
    }
}
