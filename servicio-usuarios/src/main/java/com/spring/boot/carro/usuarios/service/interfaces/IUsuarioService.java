package com.spring.boot.carro.usuarios.service.interfaces;

import com.spring.boot.carro.usuarios.persistence.enums.RolEnum;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.SaldoResponseDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioResponseDTO;

import java.util.List;

public interface IUsuarioService {

    public List<UsuarioResponseDTO> listar();

    public UsuarioResponseDTO obtenerPorId(Long id);

    public UsuarioResponseDTO crear(UsuarioRequestDTO usuarioRequestDTO);

    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO usuarioRequestDTO);

    // Cambia solo el rol de un usuario existente (endpoint PATCH, restringido a ADMIN).
    public UsuarioResponseDTO cambiarRol(Long id, RolEnum nuevoRol);

    public void eliminar(Long id);

    // --- Saldo de minutos (flujos cruzados SOA) ---
    public SaldoResponseDTO obtenerSaldo(Long id);

    public SaldoResponseDTO cargarSaldo(Long id, Integer minutos);

}
