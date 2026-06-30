package com.spring.boot.carro.usuarios.presentation.controller;

import com.spring.boot.carro.usuarios.presentation.dto.usuario.CambiarRolRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.CargarSaldoRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.DatosBasicosUsuarioDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.SaldoResponseDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.usuarios.service.interfaces.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO, @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuarioRequestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // Endpoint interno: datos basicos del usuario para llamadas inter-servicio (lo consume
    // Reservas via el bus, p. ej. el instructor viendo a sus alumnos). Abierto como /saldo.
    @GetMapping("/{id}/datos-basicos")
    public ResponseEntity<DatosBasicosUsuarioDTO> obtenerDatosBasicos(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerDatosBasicos(id));
    }

    // Cambiar el rol de un usuario. Solo un ADMIN puede hacerlo (@PreAuthorize -> 403 si no lo es).
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(@PathVariable Long id,
                                                         @RequestBody @Valid CambiarRolRequestDTO request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request.rol()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO, UriComponentsBuilder uriComponentsBuilder) {
        UsuarioResponseDTO creado = usuarioService.crear(usuarioRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    // --- Saldo de minutos (endpoints inter-servicio para los flujos cruzados SOA) ---

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoResponseDTO> obtenerSaldo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerSaldo(id));
    }

    @PostMapping("/{id}/saldo/cargar")
    public ResponseEntity<SaldoResponseDTO> cargarSaldo(@PathVariable Long id, @RequestBody @Valid CargarSaldoRequestDTO request) {
        return ResponseEntity.ok(usuarioService.cargarSaldo(id, request.minutos()));
    }
}
