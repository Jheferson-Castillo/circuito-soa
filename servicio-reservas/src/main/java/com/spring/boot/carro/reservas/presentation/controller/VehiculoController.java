package com.spring.boot.carro.reservas.presentation.controller;

import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoDTO;
import com.spring.boot.carro.reservas.service.interfaces.IReservaService;
import com.spring.boot.carro.reservas.service.interfaces.IVehiculoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

    @Autowired
    private IVehiculoService vehiculoService;

    @Autowired
    private IReservaService reservaService;

    @GetMapping
    public ResponseEntity<List<VehiculoDTO>> findAll() {
        return ResponseEntity.ok(vehiculoService.listar());
    }

    @GetMapping("/operativos")
    public List<VehiculoDTO> listarVehiculosOperativos() {
        return vehiculoService.listarVehiculosOperativos();
    }


    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculoDTO>> findAllDisponibles() {
        return ResponseEntity.ok(vehiculoService.listarVehiculosDisponibles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoDTO> update(@RequestBody @Valid VehiculoDTO vehiculoDTO, @PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, vehiculoDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> create(@RequestBody @Valid VehiculoDTO vehiculoDTO, UriComponentsBuilder uriComponentsBuilder) {
        VehiculoDTO creado = vehiculoService.crear(vehiculoDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/vehiculos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PatchMapping("/reservar/{id}")
    public ResponseEntity<Void> reservarVehiculo(@PathVariable Long id) {
        vehiculoService.marcarVehiculoComoReservado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/liberar/{id}")
    public ResponseEntity<Void> liberarVehiculo(@PathVariable Long id) {
        vehiculoService.liberarVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{vehiculoId}/horarios-ocupados")
    public ResponseEntity<List<HorarioOcupadoDTO>> getHorariosOcupados(
            @PathVariable Long vehiculoId
    ) {
        return ResponseEntity.ok(reservaService.listarHorariosOcupados(vehiculoId));
    }

    @GetMapping("/{pagoId}/horarios-ocupados-cliente")
    public ResponseEntity<List<HorarioOcupadoDTO>> horariosCliente(
            @PathVariable Long pagoId) {

        return ResponseEntity.ok(reservaService.obtenerHorariosCliente(pagoId));
    }


}
