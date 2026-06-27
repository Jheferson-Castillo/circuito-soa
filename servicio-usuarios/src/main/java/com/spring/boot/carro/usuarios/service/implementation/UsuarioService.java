package com.spring.boot.carro.usuarios.service.implementation;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import com.spring.boot.carro.usuarios.persistence.repository.UsuarioRepository;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.SaldoResponseDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.usuarios.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.usuarios.service.exception.BusinessException;
import com.spring.boot.carro.usuarios.service.exception.NotFoundException;
import com.spring.boot.carro.usuarios.service.interfaces.IUsuarioService;
import com.spring.boot.carro.usuarios.util.mapper.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String NOT_FOUND_MSG = "Usuario no encontrado con el id: ";

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> listar() {
        return usuarioMapper.toResponseList(usuarioRepository.findByActivoTrue());
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        return usuarioMapper.toResponse(entity);
    }

    @Transactional
    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO usuarioRequestDTO) {

        usuarioRepository.findByNumeroDocumento(usuarioRequestDTO.getNumeroDocumento())
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe un usuario con el número de documento: "
                            + usuarioRequestDTO.getNumeroDocumento());
                });

        Usuario entity = usuarioMapper.toEntity(usuarioRequestDTO);
        entity.setFechaRegistro(LocalDateTime.now());

        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    @Transactional
    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));

        usuarioRepository.findByNumeroDocumento(usuarioRequestDTO.getNumeroDocumento())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe otro usuario con el número de documento: "
                            + usuarioRequestDTO.getNumeroDocumento());
                });

        usuarioRepository.findByEmail(usuarioRequestDTO.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe otro usuario con el correo: "
                            + usuarioRequestDTO.getEmail());
                });

        usuarioMapper.updateEntityFromDto(usuarioRequestDTO, entity);
        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        entity.setActivo(false);
        usuarioRepository.save(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public SaldoResponseDTO obtenerSaldo(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        int saldo = entity.getSaldoMinutos() == null ? 0 : entity.getSaldoMinutos();
        return new SaldoResponseDTO(entity.getId(), saldo);
    }

    /**
     * Suma minutos al saldo del usuario (flujo A: comprar paquete -> cargar horas).
     */
    @Transactional
    @Override
    public SaldoResponseDTO cargarSaldo(Long id, Integer minutos) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        int actual = entity.getSaldoMinutos() == null ? 0 : entity.getSaldoMinutos();
        entity.setSaldoMinutos(actual + minutos);
        usuarioRepository.save(entity);
        return new SaldoResponseDTO(entity.getId(), entity.getSaldoMinutos());
    }
}
