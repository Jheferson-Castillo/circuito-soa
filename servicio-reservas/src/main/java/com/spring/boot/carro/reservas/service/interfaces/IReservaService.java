package com.spring.boot.carro.reservas.service.interfaces;

import com.spring.boot.carro.reservas.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.IncidenciaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.PagoMinutosDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.ReprogramacionRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.AlumnoConClasesDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {

    // idInstructor (del claim "uid") se usa para el control de propiedad: solo el instructor asignado.
    public ReservaResponseDTO registrarIncidencia(Long reservaId, IncidenciaRequestDTO incidenciaRequestDTO, Long idInstructor);

    public ReservaResponseDTO reprogramarReserva(Long reservaId, ReprogramacionRequestDTO dto);

    // El ADMIN asigna (o reasigna) el instructor de una reserva.
    public ReservaResponseDTO asignarInstructor(Long reservaId, Long idInstructor);

    // Agenda del instructor autenticado: solo las reservas que tiene asignadas (próximas primero).
    public List<ReservaResponseDTO> misReservas(Long idInstructor);

    // El instructor finaliza una clase SUYA (estado -> FINALIZADO). idInstructor proviene del token.
    public ReservaResponseDTO finalizarReserva(Long reservaId, Long idInstructor);

    // Alumnos del instructor: agrupa sus reservas por alumno (idUsuario) y trae los datos basicos
    // de cada uno desde Usuarios (via bus), propagando el token. idInstructor proviene del token.
    public List<AlumnoConClasesDTO> misAlumnos(Long idInstructor, String bearerToken);

    // Incidencias de una reserva, con alcance segun el rol del token (ADMIN/INSTRUCTOR/ALUMNO).
    public List<IncidenciaDTO> verIncidencias(Long reservaId, Long uid, String rol);

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
