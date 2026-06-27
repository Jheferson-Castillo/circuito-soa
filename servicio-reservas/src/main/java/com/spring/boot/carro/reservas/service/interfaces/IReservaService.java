package com.spring.boot.carro.reservas.service.interfaces;

import com.spring.boot.carro.reservas.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.PagoMinutosDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.ReprogramacionRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {

    public ReservaResponseDTO registrarIncidencia(Long reservaId, IncidenciaRequestDTO incidenciaRequestDTO);

    public ReservaResponseDTO reprogramarReserva(Long reservaId, ReprogramacionRequestDTO dto);

    public void cancelarReserva(Long reservaId);

    public PagoMinutosDTO detalleMinutos(Long pagoId);

    public DetalleReservaResponseDTO detalleReserva(Long id);

    public ReservaResponseDTO crearReserva(ReservaRequestDTO request);

    public List<ReservaResponseDTO> listar();

    public List<HorarioOcupadoDTO> listarCalendario();

    public List<HorarioOcupadoDTO> listarHorariosOcupados(Long vehiculoId);

    public List<HorarioOcupadoDTO> obtenerHorariosCliente(Long clienteId);

    public List<HorarioOcupadoDTO> obtenerHorarios(Long vehiculoId, Long pagoId);
}
