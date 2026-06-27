package com.spring.boot.carro.pagos.presentation.controller;

import com.spring.boot.carro.pagos.presentation.dto.paquete.PaqueteDTO;
import com.spring.boot.carro.pagos.service.interfaces.IPaqueteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/paquetes")
public class PaqueteController {

    @Autowired
    private IPaqueteService paqueteService;


    @GetMapping
    public ResponseEntity<List<PaqueteDTO>> findAll() {
        return ResponseEntity.ok(paqueteService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteDTO> update(@RequestBody @Valid PaqueteDTO paqueteDTO, @PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.actualizar(id, paqueteDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<PaqueteDTO> create(@RequestBody @Valid PaqueteDTO paqueteDTO, UriComponentsBuilder uriComponentsBuilder) {
        PaqueteDTO creado = paqueteService.crear(paqueteDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/paquetes/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }


    @GetMapping("/{id}/tiene-pagos")
    public ResponseEntity<Boolean> verificarPagos(@PathVariable Long id) {
        boolean tienePagos = paqueteService.tienePagosAsociados(id);
        return ResponseEntity.ok(tienePagos);
    }
}
