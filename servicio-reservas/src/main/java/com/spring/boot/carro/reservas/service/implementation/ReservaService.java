package com.spring.boot.carro.reservas.service.implementation;

import com.spring.boot.carro.reservas.persistence.entity.*;
import com.spring.boot.carro.reservas.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.reservas.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.enums.TipoEventoReservaEnum;
import com.spring.boot.carro.reservas.persistence.projection.HorarioOcupadoProjection;
import com.spring.boot.carro.reservas.persistence.repository.*;
import com.spring.boot.carro.reservas.presentation.dto.reserva.evento.*;
import com.spring.boot.carro.reservas.presentation.dto.reserva.ReservaMinutosDTO;
import com.spring.boot.carro.reservas.presentation.dto.reserva.*;
import com.spring.boot.carro.reservas.service.client.PagoClienteGateway;
import com.spring.boot.carro.reservas.service.client.PagoInfoDTO;
import com.spring.boot.carro.reservas.service.client.UsuarioDatosClient;
import com.spring.boot.carro.reservas.service.client.UsuarioSaldoClient;
import com.spring.boot.carro.reservas.service.exception.BusinessException;
import com.spring.boot.carro.reservas.service.exception.NotFoundException;
import com.spring.boot.carro.reservas.service.interfaces.IReservaService;
import com.spring.boot.carro.reservas.service.scheduler.ReservaJobSchedulerService;
import com.spring.boot.carro.reservas.util.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService implements IReservaService {

    private final ReservaRepository reservaRepository;
    // El dato del pago se pide al servicio de Pagos via el bus (gateway real, Fase 6).
    private final PagoClienteGateway pagoGateway;
    // Saldo del usuario: se consulta al servicio de Usuarios via el bus (flujo B).
    private final UsuarioSaldoClient usuarioSaldoClient;
    // Datos basicos del alumno: se consultan a Usuarios via el bus (propagando el token).
    private final UsuarioDatosClient usuarioDatosClient;
    private final ReservaMapper reservaMapper;
    private final VehiculoRepository vehiculoRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final ReservaJobSchedulerService reservaJobSchedulerService;
    private final String NOT_FOUND_MSG = "No se encontró ";
    private final String PAGO = "pago";
    private final String VEHICULO = "vehículo";
    private final String RESERVA = "reserva";

    //  CONSTANTES DE NEGOCIO
    private final int MAX_RESERVAS_SIMULTANEAS = 8;
    private final int MAX_REPROGRAMACIONES_PERMITIDAS = 2;
    private final int TOLERANCIA_MINUTOS = 1;

    private final int MIN_RESERVA_MINUTOS = 60;        // mínimo 1 hora
    private final int MAX_RESERVA_MINUTOS = 300;    // máximo 5 horas
    private final int MIN_ANTICIPACION_MINUTOS = 1;//eran 5
    private final int MAX_ANTICIPACION_DIAS = 20;

    /**
     * El ADMIN asigna (o reasigna) el instructor de una reserva. El instructor es un Usuario
     * (rol INSTRUCTOR) que vive en el servicio de Usuarios; aqui solo guardamos su id.
     */
    @Transactional
    @Override
    public ReservaResponseDTO asignarInstructor(Long reservaId, Long idInstructor) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + RESERVA + " con id: " + reservaId));
        reserva.setIdInstructor(idInstructor);
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    /**
     * Devuelve la agenda del instructor autenticado: solo las reservas que tiene asignadas,
     * ordenadas por fecha (las próximas primero). El idInstructor proviene del claim "uid".
     */
    @Transactional(readOnly = true)
    @Override
    public List<ReservaResponseDTO> misReservas(Long idInstructor) {
        return reservaMapper.toResponseList(
                reservaRepository.findByIdInstructorOrderByFechaReservaAsc(idInstructor));
    }

    /**
     * El instructor autenticado finaliza una clase SUYA (estado -> FINALIZADO).
     * - Control de propiedad: el idInstructor de la reserva debe coincidir con el uid del token.
     * - No se puede finalizar una reserva ya FINALIZADO o CANCELADO.
     */
    @Transactional
    @Override
    public ReservaResponseDTO finalizarReserva(Long reservaId, Long idInstructor) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + RESERVA + " con id: " + reservaId));

        // Control de propiedad: solo el instructor asignado a la reserva puede finalizarla.
        if (reserva.getIdInstructor() == null || !reserva.getIdInstructor().equals(idInstructor)) {
            throw new AccessDeniedException("No puedes finalizar una clase que no te fue asignada");
        }

        if (reserva.getEstado() == EstadoReservaEnum.FINALIZADO) {
            throw new BusinessException("La reserva ya está finalizada.");
        }
        if (reserva.getEstado() == EstadoReservaEnum.CANCELADO) {
            throw new BusinessException("No se puede finalizar una reserva cancelada.");
        }

        reserva.setEstado(EstadoReservaEnum.FINALIZADO);
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    /**
     * Alumnos del instructor autenticado: toma sus reservas asignadas, las agrupa por alumno
     * (resolviendo idPago -> idUsuario via Pagos) y enriquece cada alumno con sus datos basicos
     * traidos de Usuarios (via bus, propagando el token). Degrada con gracia si algun servicio
     * remoto no responde: muestra el idUsuario aunque falten los datos personales.
     */
    @Transactional(readOnly = true)
    @Override
    public List<AlumnoConClasesDTO> misAlumnos(Long idInstructor, String bearerToken) {
        List<Reserva> reservas = reservaRepository.findByIdInstructorOrderByFechaReservaAsc(idInstructor);

        // Agrupar por alumno (idUsuario) conservando el orden por fecha (LinkedHashMap).
        Map<Long, List<Reserva>> reservasPorAlumno = new LinkedHashMap<>();
        Map<Long, Long> cachePagoUsuario = new HashMap<>();   // evita reconsultar el mismo pago
        for (Reserva r : reservas) {
            Long idUsuario = cachePagoUsuario.get(r.getIdPago());
            if (idUsuario == null) {
                idUsuario = resolverIdUsuario(r.getIdPago());
                if (idUsuario == null) {
                    continue;   // no se pudo atribuir el alumno (Pagos no respondio): se omite
                }
                cachePagoUsuario.put(r.getIdPago(), idUsuario);
            }
            reservasPorAlumno.computeIfAbsent(idUsuario, k -> new ArrayList<>()).add(r);
        }

        // Para cada alumno: enriquecer con sus datos basicos (Usuarios via bus) y mapear sus clases.
        List<AlumnoConClasesDTO> alumnos = new ArrayList<>();
        for (Map.Entry<Long, List<Reserva>> entry : reservasPorAlumno.entrySet()) {
            Long idUsuario = entry.getKey();
            List<ReservaResponseDTO> clases = reservaMapper.toResponseList(entry.getValue());
            UsuarioDatosClient.DatosBasicosDTO datos =
                    usuarioDatosClient.obtenerDatosBasicos(idUsuario, bearerToken);
            if (datos != null) {
                alumnos.add(new AlumnoConClasesDTO(idUsuario, datos.nombre(), datos.apellido(),
                        datos.email(), datos.telefono(), clases));
            } else {
                // Degradacion: Usuarios no respondio -> solo id y clases.
                alumnos.add(new AlumnoConClasesDTO(idUsuario, null, null, null, null, clases));
            }
        }
        return alumnos;
    }

    // Resuelve el alumno (idUsuario) a partir del idPago preguntando a Pagos (via bus).
    // Degrada con gracia: devuelve null si Pagos/bus no responde.
    private Long resolverIdUsuario(Long idPago) {
        try {
            PagoInfoDTO info = pagoGateway.obtenerPagoInfo(idPago);
            return info != null ? info.idUsuario() : null;
        } catch (Exception e) {
            log.warn("mis-alumnos: no se pudo resolver el alumno del pago {} ({})", idPago, e.getMessage());
            return null;
        }
    }

    /**
     * Incidencias (EventoReserva tipo INCIDENCIA) de una reserva, con alcance segun el rol del token:
     *  - ADMIN: cualquier reserva.
     *  - INSTRUCTOR: solo si la reserva le fue asignada (idInstructor == uid).
     *  - ALUMNO: solo si la reserva es suya (idUsuario dueño == uid, resuelto idPago->idUsuario via Pagos).
     * Si no hay incidencias, devuelve lista vacia.
     */
    @Transactional(readOnly = true)
    @Override
    public List<IncidenciaDTO> verIncidencias(Long reservaId, Long uid, String rol) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + RESERVA + " con id: " + reservaId));

        verificarAccesoIncidencias(reserva, uid, rol);

        return eventoReservaRepository
                .findByReservaIdAndTipoOrderByFechaRegistroAsc(reservaId, TipoEventoReservaEnum.INCIDENCIA)
                .stream()
                .map(this::toIncidenciaDTO)
                .toList();
    }

    // Aplica el control de acceso por rol antes de devolver las incidencias.
    private void verificarAccesoIncidencias(Reserva reserva, Long uid, String rol) {
        if ("ADMIN".equals(rol)) {
            return; // el admin ve incidencias de cualquier reserva
        }
        if ("INSTRUCTOR".equals(rol)) {
            if (reserva.getIdInstructor() == null || !reserva.getIdInstructor().equals(uid)) {
                throw new AccessDeniedException("No puedes ver incidencias de una clase que no te fue asignada");
            }
            return;
        }
        if ("ALUMNO".equals(rol)) {
            Long idUsuario = resolverIdUsuario(reserva.getIdPago());
            if (idUsuario == null || !idUsuario.equals(uid)) {
                throw new AccessDeniedException("No puedes ver incidencias de una clase que no es tuya");
            }
            return;
        }
        // Rol no contemplado: denegar por defecto.
        throw new AccessDeniedException("No tienes permisos para ver estas incidencias");
    }

    private IncidenciaDTO toIncidenciaDTO(EventoReserva evento) {
        return IncidenciaDTO.builder()
                .minutosReservadosAntes(evento.getMinutosReservadosAntes())
                .minutosUsados(evento.getMinutosUsados())
                .minutosDevueltos(evento.getMinutosDevueltos())
                .minutosAfectados(evento.getMinutosAfectados())
                .detalle(evento.getDetalle())
                .fechaRegistro(evento.getFechaRegistro())
                .tipo(evento.getTipo())
                .build();
    }

    /**
     * Reglas de negocio:
     * - El pago debe estar PAGADO o PENDIENTE para poder reservar
     * - Debe tener minutos disponibles.
     * - La fecha de reserva debe ser futura y cumplir límites de tiempo.
     * - Ocho vehiculos maximos por mismo horario.
     * - La reserva se registra con estado RESERVADO.
     */
    @Transactional
    @Override
    public ReservaResponseDTO crearReserva(ReservaRequestDTO dto) {

        // 1. VALIDACIONES DEL DTO Y FECHA/DURACIÓN
        validarDatosGeneralesReserva(dto);

        // Obtener y validar Pago (servicio de Pagos via el bus)
        PagoInfoDTO pago = pagoGateway.obtenerPagoInfo(dto.getPagoId());

        validarPagoParaReserva(pago);

        // FLUJO B (crear reserva -> validar saldo): consultamos el saldo del usuario en Usuarios (via bus).
        Integer saldoUsuario = usuarioSaldoClient.obtenerSaldoMinutos(pago.idUsuario());
        if (dto.getMinutosReservados() > saldoUsuario) {
            throw new BusinessException("Saldo insuficiente. El usuario tiene " + saldoUsuario
                    + " min disponibles y la reserva requiere " + dto.getMinutosReservados() + " min.");
        }

        // 2. VALIDAR MINUTOS DISPONIBLES EN EL PAQUETE
        Integer disponibles = obtenerMinutosDisponibles(pago);

        if (disponibles < dto.getMinutosReservados()) {
            throw new BusinessException("No tienes minutos suficientes. Disponibles: " + disponibles + " min.");
        }

        // Contar reservas existentes para este pago. Si es 0, es la primera reserva de este paquete.
        boolean esPrimeraReserva = reservaRepository.countReservasByIdPago(pago.idPago()) == 0;

        // 3. LÓGICA DINÁMICA DEL MÍNIMO
        if (esPrimeraReserva) {
            if (dto.getMinutosReservados() < MIN_RESERVA_MINUTOS) {
                throw new BusinessException("La PRIMERA reserva para un paquete debe ser de al menos " + MIN_RESERVA_MINUTOS + " minutos.");
            }
        } else {

            Integer residual = disponibles - dto.getMinutosReservados();

            if (disponibles < MIN_RESERVA_MINUTOS) {
                if (dto.getMinutosReservados() < disponibles) {
                    throw new BusinessException("El saldo restante (" + disponibles + " min) es menor al mínimo (" + MIN_RESERVA_MINUTOS + " min). Debes reservar la totalidad (" + disponibles + " min) para liquidar el paquete.");
                }
            } else {
                if (dto.getMinutosReservados() < MIN_RESERVA_MINUTOS) {
                    throw new BusinessException("La duración de la reserva debe ser de al menos " + MIN_RESERVA_MINUTOS + " minutos.");
                }

                if (residual > 0 && residual < MIN_RESERVA_MINUTOS) {
                    throw new BusinessException("La reserva de " + dto.getMinutosReservados() + " minutos dejaría un saldo de " + residual + " minutos. Esto no está permitido. Debes reservar la totalidad (" + disponibles + " min) o dejar al menos " + MIN_RESERVA_MINUTOS + " minutos restantes.");
                }
            }
        }


        // Obtener y validar Vehículo
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + VEHICULO));

        validarVehiculoParaReserva(vehiculo);

        LocalDateTime inicio = dto.getFechaReserva();
        LocalDateTime fin = inicio.plusMinutes(dto.getMinutosReservados());

        // 4. VALIDACIONES DE CRUCE DE HORARIOS Y DISPONIBILIDAD

        long reservasVehiculo = reservaRepository.countCrucesVehiculo(
                0L, // ID de reserva a excluir (0L para una nueva reserva)
                vehiculo.getId(),
                inicio,
                fin
        );

        if (reservasVehiculo > 0) {
            throw new BusinessException("El vehículo seleccionado ya tiene una reserva en ese horario (" + inicio.toLocalTime() + " - " + fin.toLocalTime() + ").");
        }

        // Validar que el mismo pago no tenga otra reserva en el mismo horario.
        // (En el monolito era por usuario; ver nota en ReservaRepository, validacion por usuario -> Fase 6.)
        long reservasMismoPago = reservaRepository.countReservasIdPagoEnMismoHorario(pago.idPago(), inicio, fin);

        if (reservasMismoPago > 0) {
            throw new BusinessException("Ya tienes una reserva que se cruza con este horario.");
        }

        // Validar máximo de vehículos simultáneos en ese intervalo
        long reservasTotales = reservaRepository.countReservasEnMismoHorario(inicio, fin);

        if (reservasTotales >= MAX_RESERVAS_SIMULTANEAS) {
            throw new BusinessException("No hay disponibilidad. Máximo " + MAX_RESERVAS_SIMULTANEAS + " vehículos por horario.");
        }

        // 5. CREAR Y PERSISTIR RESERVA Y EVENTO
        Reserva reserva = reservaMapper.toEntity(dto, vehiculo);
        reserva.setEstado(EstadoReservaEnum.RESERVADO);
        reserva.setFechaRegistro(LocalDateTime.now());
        reserva.setFechaFin(fin);
        reserva.setActivo(true);

        // NOTA SOA: el email del creador venia de la sesion OAuth2 (servicio de Usuarios).
        // Aqui no hay seguridad; el bus Camel propagara el email del usuario logueado en Fase 5/6.
        reserva.setEmailCreador(null);

        reserva = reservaRepository.save(reserva);

        // Programar eventos temporales (Quartz)
        reservaJobSchedulerService.programarJobsReserva(reserva);

        // Registrar evento inicial
        EventoReserva eventoInicial = EventoReserva.builder()
                .idPago(pago.idPago())
                .reserva(reserva)
                .minutosReservadosAntes(0)
                .minutosUsados(0)
                .minutosDevueltos(0)
                .minutosAfectados(dto.getMinutosReservados())
                .detalle("Reserva creada")
                .tipo(TipoEventoReservaEnum.RESERVA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(eventoInicial);

        // Marcar vehiculo como reservado
        vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
        vehiculoRepository.save(vehiculo);

        log.info("Reserva creada id={} idPago={} vehiculoId={}", reserva.getId(), pago.idPago(), vehiculo.getId());
        return reservaMapper.toResponse(reserva);
    }


    @Override
    public List<ReservaResponseDTO> listar() {
        return reservaRepository.findByActivoTrue()
                .stream().map(reservaMapper::toResponse).toList();
    }

    @Override
    public List<HorarioOcupadoDTO> listarCalendario() {
        return reservaRepository.findByActivoTrue()
                .stream().map(reservaMapper::toResponseHorarioOcupadoDTO).toList();
    }

    @Override
    public List<HorarioOcupadoDTO>  listarHorariosOcupados(Long vehiculoId) {
        if (!vehiculoRepository.existsById(vehiculoId)){
            throw new NotFoundException("Vehículo no encontrado");
        }

        List<HorarioOcupadoProjection> data=
                reservaRepository.findHorariosOcupadosByVehiculo(vehiculoId);

        return  data.stream()
                .map(this::toHorarioOcupadoDTO)
                .toList();
    }

    @Override
    public List<HorarioOcupadoDTO> obtenerHorariosCliente(Long idPago) {
        List<HorarioOcupadoProjection> data=reservaRepository.findHorariosOcupadosPorCliente(idPago);
        return  data.stream()
                .map(this::toHorarioOcupadoDTO).toList();
    }

    @Override
    public List<HorarioOcupadoDTO> obtenerHorarios(Long vehiculoId, Long idPago) {
        // 1. Validaciones de existencia (Fail Fast)
        if (vehiculoId != null && !vehiculoRepository.existsById(vehiculoId)) {
            throw new NotFoundException("Vehículo con ID " + vehiculoId + " no encontrado");
        }

        // Validar el pago contra el servicio de Pagos (gateway -> Camel en Fase 6)
        if (idPago != null && !pagoGateway.existePago(idPago)) {
            throw new NotFoundException("Registro de pago/cliente con ID " + idPago + " no encontrado");
        }

        List<HorarioOcupadoProjection> data = reservaRepository.findHorariosOcupados(vehiculoId, idPago);

        return data.stream()
                .map(this::toHorarioOcupadoDTO)
                .toList();
    }

    // Construye el DTO de horario ocupado (sin nombre/apellido del usuario, que es dato remoto).
    private HorarioOcupadoDTO toHorarioOcupadoDTO(HorarioOcupadoProjection h) {
        return new HorarioOcupadoDTO(
                h.getIdReserva(),
                h.getInicio(),
                h.getFin(),
                h.getIdPago(),
                h.getIdVehiculo(),
                h.getEstado(),
                h.getPlacaVehiculo(),
                h.getMinutosReservados()
        );
    }


    /**
     * Registra una incidencia, calcula el tiempo real usado y devuelve la diferencia al saldo.
     */
    @Transactional
    @Override
    public ReservaResponseDTO registrarIncidencia(Long reservaId, IncidenciaRequestDTO incidenciaRequestDTO, Long idInstructor) {

        Reserva reserva = obtenerReserva(reservaId);

        // Control de propiedad: solo el instructor asignado a la reserva puede registrar incidencias.
        if (reserva.getIdInstructor() == null || !reserva.getIdInstructor().equals(idInstructor)) {
            throw new AccessDeniedException("No puedes registrar una incidencia en una clase que no te fue asignada");
        }

        if (reserva.getEstado() != EstadoReservaEnum.EN_PROGRESO) {
            throw new BusinessException("Solo reservas EN_PROGRESO pueden registrar incidencias.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        long minutosTranscurridos = Duration.between(reserva.getFechaReserva(), ahora).toMinutes();
        long minutosUsadosCalc = Math.max(0, minutosTranscurridos - TOLERANCIA_MINUTOS);

        int minutosReservadosAntes = reserva.getMinutosReservados();
        int minutosUsados = (int) Math.min(minutosUsadosCalc, minutosReservadosAntes);

        int devueltos = minutosReservadosAntes - minutosUsados;
        int minutosAfectados = -devueltos;

        if (devueltos > 0) {
            EventoReserva incidencia = EventoReserva.builder()
                    .idPago(reserva.getIdPago())
                    .reserva(reserva)
                    .minutosReservadosAntes(minutosReservadosAntes)
                    .minutosUsados(minutosUsados)
                    .minutosDevueltos(devueltos)
                    .minutosAfectados(minutosAfectados)
                    .detalle("Incidencia registrada: " + incidenciaRequestDTO.getDetalle() + " | Devolución: " + devueltos + " min.")
                    .tipo(TipoEventoReservaEnum.INCIDENCIA)
                    .fechaRegistro(ahora)
                    .build();

            eventoReservaRepository.save(incidencia);
        } else {
            EventoReserva incidencia = EventoReserva.builder()
                    .idPago(reserva.getIdPago())
                    .reserva(reserva)
                    .minutosReservadosAntes(minutosReservadosAntes)
                    .minutosUsados(minutosUsados)
                    .minutosDevueltos(0)
                    .minutosAfectados(0)
                    .detalle("Incidencia sin devolución de saldo: " + incidenciaRequestDTO.getDetalle())
                    .tipo(TipoEventoReservaEnum.INCIDENCIA)
                    .fechaRegistro(ahora)
                    .build();
            eventoReservaRepository.save(incidencia);
        }

        reserva.setMinutosReservados(minutosUsados);
        reserva.setEstado(EstadoReservaEnum.INCIDENCIA);
        reserva.setActivo(false);
        reservaRepository.save(reserva);

        reservaJobSchedulerService.eliminarJobsReserva(reservaId);

        gestionarLiberacionVehiculo(reserva.getVehiculo(), reserva.getId());

        log.info("Incidencia registrada reservaId={} usados={} devueltos={}", reservaId, minutosUsados, devueltos);
        return reservaMapper.toResponse(reserva);
    }

    /**
     * Reprograma una reserva existente (cambio de fecha, duración o vehículo).
     */
    @Transactional
    public ReservaResponseDTO reprogramarReserva(Long reservaId, ReprogramacionRequestDTO dto) {

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReservaEnum.RESERVADO) {
            throw new BusinessException("Solo reservas en estado RESERVADO pueden reprogramarse.");
        }

        validarDatosReprogramacion(dto);

        LocalDateTime inicioNuevo = dto.getNuevaFecha();
        LocalDateTime finNuevo = inicioNuevo.plusMinutes(dto.getMinutosReservados());
        Long idPago = reserva.getIdPago();

        boolean esMismoHorario = reserva.getFechaReserva().equals(inicioNuevo) &&
                reserva.getMinutosReservados().equals(dto.getMinutosReservados());

        boolean esMismoVehiculo = reserva.getVehiculo().getId().equals(dto.getVehiculoId());
        if (esMismoHorario && esMismoVehiculo) {
            throw new BusinessException("La nueva programación es idéntica a la actual. No se requiere reprogramación.");
        }

        long totalReprog = eventoReservaRepository.countReprogramaciones(reservaId);
        if (totalReprog >= MAX_REPROGRAMACIONES_PERMITIDAS) {
            throw new BusinessException("Solo se permiten " + MAX_REPROGRAMACIONES_PERMITIDAS + " reprogramaciones por reserva.");
        }

        Vehiculo vehiculoAnterior = reserva.getVehiculo();
        Vehiculo vehiculoNuevo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + " vehículo con id: " + dto.getVehiculoId()));

        validarVehiculoParaReserva(vehiculoNuevo);

        long reservasVehiculo = reservaRepository.countCrucesVehiculo(reservaId, dto.getVehiculoId(), inicioNuevo, finNuevo);

        if (reservasVehiculo > 0) {
            throw new BusinessException("El vehículo seleccionado ya tiene una reserva en ese horario (" + inicioNuevo.toLocalTime() + " - " +finNuevo.toLocalTime() + ").");
        }

        long reservasMismoPago = reservaRepository.countCrucesIdPagoExcluyendoActual(reservaId, idPago, inicioNuevo, finNuevo);

        if (reservasMismoPago > 0) {
            throw new BusinessException("Ya tienes otra reserva que se cruza con ese horario.");
        }

        long reservasTotales = reservaRepository.countCrucesHorarioExcluyendoActual(reservaId, inicioNuevo, finNuevo);

        if (reservasTotales >= MAX_RESERVAS_SIMULTANEAS) {
            throw new BusinessException("No hay disponibilidad. Máximo " + MAX_RESERVAS_SIMULTANEAS + " reservas simultáneas para ese horario.");
        }

        int minutosActuales = reserva.getMinutosReservados();
        int minutosNuevos = dto.getMinutosReservados();

        int diferencia = minutosNuevos - minutosActuales; // + si aumenta, - si reduce

        if (diferencia > 0) {
            // Si la duración aumenta, validar si el saldo restante del paquete cubre la diferencia.
            PagoInfoDTO pago = pagoGateway.obtenerPagoInfo(idPago);
            Integer disponibles = obtenerMinutosDisponibles(pago);
            if (diferencia > disponibles) {
                throw new BusinessException("No tienes saldo suficiente para incrementar la reserva. Saldo disponible: " + disponibles + " min. Diferencia requerida: " + diferencia + " min.");
            }
        }

        LocalDateTime fechaAnterior = reserva.getFechaReserva();
        Long vehiculoAntesId = vehiculoAnterior.getId();

        reserva.setFechaReserva(inicioNuevo);
        reserva.setFechaFin(finNuevo);
        reserva.setMinutosReservados(minutosNuevos);
        reserva.setVehiculo(vehiculoNuevo);

        reservaRepository.save(reserva);

        reservaJobSchedulerService.programarJobsReserva(reserva);

        if (!esMismoVehiculo) {
            gestionarLiberacionVehiculo(vehiculoAnterior, reserva.getId());

            vehiculoNuevo.setEstado(EstadoVehiculosEnum.RESERVADO);
            vehiculoRepository.save(vehiculoNuevo);
        } else {
            if (vehiculoNuevo.getEstado() == EstadoVehiculosEnum.DISPONIBLE) {
                vehiculoNuevo.setEstado(EstadoVehiculosEnum.RESERVADO);
                vehiculoRepository.save(vehiculoNuevo);
            }
        }

        Integer minutosUsadosEvento = 0;
        Integer minutosDevueltosEvento = 0;
        String detalleAdicional = "";

        if (diferencia > 0) {
            minutosUsadosEvento = diferencia;
            detalleAdicional = " (Aumento: +" + diferencia + " min)";
        } else if (diferencia < 0) {
            minutosDevueltosEvento = Math.abs(diferencia);
            detalleAdicional = " (Devolución: -" + minutosDevueltosEvento + " min)";
        }

        String detalleVehiculo = vehiculoAntesId.equals(dto.getVehiculoId())
                ? ""
                : " | Vehículo: antes " + vehiculoAntesId + " ahora " + dto.getVehiculoId();

        String detalleCompleto = "Reprogramada: antes " + fechaAnterior +
                " ahora " + inicioNuevo + detalleAdicional + detalleVehiculo;

        EventoReserva evento = EventoReserva.builder()
                .idPago(idPago)
                .reserva(reserva)
                .minutosReservadosAntes(minutosActuales)
                .minutosUsados(minutosUsadosEvento)
                .minutosDevueltos(minutosDevueltosEvento)
                .minutosAfectados(diferencia)
                .detalle(detalleCompleto)
                .numeroReprogramacion((int) totalReprog + 1)
                .tipo(TipoEventoReservaEnum.REPROGRAMADA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(evento);

        log.info("Reserva reprogramada id={} antes={} ahora={}", reserva.getId(), fechaAnterior, inicioNuevo);

        return reservaMapper.toResponse(reserva);
    }

    /**
     * Cancela una reserva que aún no ha comenzado y devuelve la totalidad de los minutos.
     */
    @Transactional
    @Override
    public void cancelarReserva(Long reservaId) {

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReservaEnum.CANCELADO) {
            throw new BusinessException("La reserva ya se encuentra cancelada.");
        }

        if (reserva.getEstado() != EstadoReservaEnum.RESERVADO) {
            throw new BusinessException("Solo reservas en estado RESERVADO pueden cancelarse. Estado actual: " + reserva.getEstado());
        }

        if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La reserva ya ha comenzado y no puede cancelarse. Utiliza el proceso de INCIDENCIA");
        }

        reserva.setEstado(EstadoReservaEnum.CANCELADO);
        reserva.setActivo(false);
        reservaRepository.save(reserva);

        reservaJobSchedulerService.eliminarJobsReserva(reservaId);

        int minutosADevolver = reserva.getMinutosReservados();

        EventoReserva eventoCancelacion = EventoReserva.builder()
                .idPago(reserva.getIdPago())
                .reserva(reserva)
                .minutosReservadosAntes(minutosADevolver)
                .minutosUsados(0)
                .minutosDevueltos(minutosADevolver)
                .minutosAfectados(-minutosADevolver)
                .detalle("Devolución total por cancelación a tiempo.")
                .tipo(TipoEventoReservaEnum.CANCELADO)
                .fechaRegistro(LocalDateTime.now())
                .build();
        eventoReservaRepository.save(eventoCancelacion);

        gestionarLiberacionVehiculo(reserva.getVehiculo(), reserva.getId());

        log.info("Reserva cancelada id={} devolución={}", reservaId, reserva.getMinutosReservados());
    }


    /**
     * Resumen del consumo de minutos para un pago específico.
     */
    @Override
    @Transactional(readOnly = true)
    public PagoMinutosDTO detalleMinutos(Long pagoId) {
        // Datos del paquete (duracion) provienen del servicio de Pagos (gateway -> Camel en Fase 6).
        PagoInfoDTO pago = pagoGateway.obtenerPagoInfo(pagoId);

        Integer consumidos = obtenerMinutosConsumidos(pagoId);

        int minutosTotales = pago.duracionMinutosPaquete();
        int disponibles = minutosTotales - consumidos;

        List<ReservaMinutosDTO> reservas = reservaRepository.findByIdPago(pagoId)
                .stream()
                .map(r -> {
                    List<EventoReserva> consumos = eventoReservaRepository.findByReservaIdOrderByFechaRegistroAsc(r.getId());
                    List<IncidenciaDTO> incidencias = consumos.stream()
                            .map(c -> IncidenciaDTO.builder()
                                    .minutosReservadosAntes(c.getMinutosReservadosAntes())
                                    .minutosUsados(c.getMinutosUsados())
                                    .minutosDevueltos(c.getMinutosDevueltos())
                                    .minutosAfectados(c.getMinutosAfectados())
                                    .detalle(c.getDetalle())
                                    .fechaRegistro(c.getFechaRegistro())
                                    .tipo(c.getTipo())
                                    .build()
                            ).toList();

                    return ReservaMinutosDTO.builder()
                            .reservaId(r.getId())
                            .fechaReserva(r.getFechaReserva())
                            .fechaFin(r.getFechaFin())
                            .minutosReservados(r.getMinutosReservados())
                            .estado(r.getEstado())
                            .detalle(incidencias.isEmpty() ? Collections.emptyList() : incidencias)
                            .build();
                }).toList();

        return PagoMinutosDTO.builder()
                .pagoId(pagoId)
                .minutosTotalesPaquete(minutosTotales)
                .minutosConsumidos(consumidos)
                .minutosDisponibles(Math.max(disponibles, 0))
                .reservas(reservas)
                .build();
    }

    @Override
    public DetalleReservaResponseDTO detalleReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        return reservaMapper.toDetalleDTO(reserva);
    }

    /**
     * Valida si el pago puede crear reservas.
     */
    private void validarPagoParaReserva(PagoInfoDTO pago) {
        if (pago.estado() == EstadoPagoEnum.CANCELADO)
            throw new BusinessException("El pago está cancelado.");
        if (!(pago.estado() == EstadoPagoEnum.PAGADO || pago.estado() == EstadoPagoEnum.PENDIENTE))
            throw new BusinessException("Estado de pago no permite reservas: " + pago.estado());
    }

    /**
     * Valida el estado del vehículo antes de usarlo en una reserva.
     */
    private void validarVehiculoParaReserva(Vehiculo v) {
        if (v.getEstado() == EstadoVehiculosEnum.MANTENIMIENTO)
            throw new BusinessException("Vehículo en mantenimiento.");
    }

    /**
     * Suma total de minutos consumidos/devueltos del paquete (a partir de EventoReserva, dato local).
     */
    private Integer obtenerMinutosConsumidos(Long idPago) {
        Integer consumidos = eventoReservaRepository.sumMinutosAfectadosByIdPago(idPago);
        return consumidos == null ? 0 : consumidos;
    }

    /**
     * Minutos disponibles = duracion del paquete (remoto) - consumidos (local).
     */
    private Integer obtenerMinutosDisponibles(PagoInfoDTO pago) {
        if (pago == null) throw new IllegalArgumentException("Pago no puede ser null");

        Integer consumidos = obtenerMinutosConsumidos(pago.idPago());
        int minutosTotales = pago.duracionMinutosPaquete();
        int disponibles = minutosTotales - consumidos;

        return Math.max(disponibles, 0);
    }

    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + RESERVA));
    }

    /**
     * Verifica si existen otras reservas para decidir el estado final del vehículo.
     */
    private void gestionarLiberacionVehiculo(Vehiculo vehiculo, Long reservaActualId) {
        boolean tieneMasReservas = reservaRepository.existsOtrasReservasActivas(vehiculo.getId(), reservaActualId);

        if (tieneMasReservas) {
            vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
            log.info("Vehículo ID: {} permanece RESERVADO por otras reservas pendientes.", vehiculo.getId());
        } else {
            vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            log.info("Vehículo ID: {} ahora está DISPONIBLE.", vehiculo.getId());
        }

        vehiculoRepository.save(vehiculo);
    }

    /**
     * Valida la duración máxima y límites de anticipación para la creación de una reserva.
     */
    private void validarDatosGeneralesReserva(ReservaRequestDTO dto) {

        if (dto.getMinutosReservados() == null || dto.getMinutosReservados() <= 0 || dto.getMinutosReservados() > MAX_RESERVA_MINUTOS) {
            throw new BusinessException("La duración de la reserva debe ser positiva y no puede exceder los " + MAX_RESERVA_MINUTOS + " minutos.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ahoraConMargen = ahora.plusMinutes(MIN_ANTICIPACION_MINUTOS);

        if (dto.getFechaReserva().isBefore(ahoraConMargen)) {
            throw new BusinessException("La reserva debe ser al menos con " + MIN_ANTICIPACION_MINUTOS + " minutos de anticipación.");
        }

        if (dto.getFechaReserva().isAfter(ahora.plusDays(MAX_ANTICIPACION_DIAS))) {
            throw new BusinessException("No se permiten reservas con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }

    /**
     * Valida la duración y límites de anticipación para la reprogramación.
     */
    private void validarDatosReprogramacion(ReprogramacionRequestDTO dto) {

        if (dto.getMinutosReservados() == null || dto.getMinutosReservados() < MIN_RESERVA_MINUTOS || dto.getMinutosReservados() > MAX_RESERVA_MINUTOS) {
            throw new BusinessException("La duración de la reserva debe ser entre " + MIN_RESERVA_MINUTOS + " y " + MAX_RESERVA_MINUTOS + " minutos.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ahoraConMargen = ahora.plusMinutes(MIN_ANTICIPACION_MINUTOS);

        if (dto.getNuevaFecha().isBefore(ahoraConMargen)) {
            throw new BusinessException("La reserva debe ser al menos con " + MIN_ANTICIPACION_MINUTOS + " minutos de anticipación.");
        }

        if (dto.getNuevaFecha().isAfter(ahora.plusDays(MAX_ANTICIPACION_DIAS))) {
            throw new BusinessException("No se permiten reservas con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }


}
