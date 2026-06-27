package com.spring.boot.carro.reservas.service.interfaces;

import com.spring.boot.carro.reservas.presentation.dto.vehiculo.VehiculoDTO;

import java.util.List;

public interface IVehiculoService {

    public List<VehiculoDTO> listar();

    public List<VehiculoDTO> listarVehiculosOperativos() ;

    public VehiculoDTO obtenerPorId(Long id);

    public VehiculoDTO crear(VehiculoDTO vehiculoDTO);

    public VehiculoDTO actualizar(Long id, VehiculoDTO vehiculoDTO);

    public void eliminar(Long id);

    public List<VehiculoDTO> listarVehiculosDisponibles();

    public void marcarVehiculoComoReservado(Long id);

    public void liberarVehiculo(Long id);
}
