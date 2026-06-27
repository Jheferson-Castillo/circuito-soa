package com.spring.boot.carro.reservas.service.implementation;

import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.repository.VehiculoRepository;
import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoDTO;
import com.spring.boot.carro.reservas.service.exception.BusinessException;
import com.spring.boot.carro.reservas.service.exception.NotFoundException;
import com.spring.boot.carro.reservas.service.interfaces.IVehiculoService;
import com.spring.boot.carro.reservas.util.mapper.VehiculoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService implements IVehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private VehiculoMapper vehiculoMapper;

    private final String NOT_FOUND_MSG = "Vehiculo no encontrado con el id: ";

    @Transactional(readOnly = true)
    @Override
    public List<VehiculoDTO> listar() {
        return vehiculoMapper.toResponseList(vehiculoRepository.findByActivoTrue());
    }

    @Override
    public List<VehiculoDTO> listarVehiculosOperativos() {
        List<Vehiculo> lista = vehiculoRepository
                .findByActivoTrueAndEstadoNot(EstadoVehiculosEnum.MANTENIMIENTO);

        return lista.stream()
                .map(vehiculoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public VehiculoDTO obtenerPorId(Long id) {
        Vehiculo entity = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG+ id));
        return vehiculoMapper.toResponse(entity);
    }

    @Transactional
    @Override
    public VehiculoDTO crear(VehiculoDTO vehiculoDTO) {

        if (vehiculoRepository.existsByPlaca(vehiculoDTO.getPlaca())) {
            throw new BusinessException("Ya existe un vehículo registrado con la placa: " + vehiculoDTO.getPlaca());
        }

        Vehiculo entity = vehiculoMapper.toEntity(vehiculoDTO);

        if (entity.getEstado() == null) {
            entity.setEstado(EstadoVehiculosEnum.DISPONIBLE);
        }
        return vehiculoMapper.toResponse(vehiculoRepository.save(entity));
    }

    @Transactional
    @Override
    public VehiculoDTO actualizar(Long id, VehiculoDTO vehiculoDTO) {
        Vehiculo entity = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado con id: " + id));

        if (vehiculoRepository.existsByPlaca(vehiculoDTO.getPlaca()) && !entity.getPlaca().equals(vehiculoDTO.getPlaca())) {
            throw new BusinessException("Ya existe un vehículo con la placa: " + vehiculoDTO.getPlaca());
        }

        vehiculoMapper.updateEntityFromDto(vehiculoDTO, entity);

        if (vehiculoDTO.getEstado() != null) {
            switch (vehiculoDTO.getEstado()) {
                case DISPONIBLE:
                case MANTENIMIENTO:
                    entity.setEstado(vehiculoDTO.getEstado());
                    break;
                default:
                    throw new BusinessException("Estado no permitido: " + vehiculoDTO.getEstado());
            }
        }
        return vehiculoMapper.toResponse(vehiculoRepository.save(entity));
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        Vehiculo entity = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        entity.setActivo(false);
        vehiculoRepository.save(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<VehiculoDTO> listarVehiculosDisponibles() {
        List<Vehiculo> disponibles = vehiculoRepository.findByActivoTrueAndEstado(EstadoVehiculosEnum.DISPONIBLE);
        return vehiculoMapper.toResponseList(disponibles);
    }

    @Transactional
    @Override
    public void marcarVehiculoComoReservado(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado"));

        if (vehiculo.getEstado() != EstadoVehiculosEnum.DISPONIBLE) {
            throw new BusinessException("El vehículo no está disponible para reserva");
        }

        vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
        vehiculoRepository.save(vehiculo);
    }

    @Transactional
    @Override
    public void liberarVehiculo(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado"));

        vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
        vehiculoRepository.save(vehiculo);
    }
}
