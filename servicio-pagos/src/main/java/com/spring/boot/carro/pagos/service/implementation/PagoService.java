package com.spring.boot.carro.pagos.service.implementation;

import com.spring.boot.carro.pagos.persistence.entity.*;
import com.spring.boot.carro.pagos.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.pagos.persistence.enums.TipoPagoEnum;
import com.spring.boot.carro.pagos.persistence.repository.*;
import com.spring.boot.carro.pagos.presentation.dto.pago.*;
import com.spring.boot.carro.pagos.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.pagos.service.client.UsuarioSaldoClient;
import com.spring.boot.carro.pagos.service.exception.BusinessException;
import com.spring.boot.carro.pagos.service.exception.NotFoundException;
import com.spring.boot.carro.pagos.service.interfaces.IPagoService;
import com.spring.boot.carro.pagos.util.mapper.PagoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService implements IPagoService {

    private final String NOT_FOUND_MSG = "Pago no encontrado con el id: ";

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private PagoMapper pagoMapper;

    @Autowired
    private UsuarioSaldoClient usuarioSaldoClient;

    @Transactional(readOnly = true)
    @Override
    public List<PagoListadoResponseDTO> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toListadoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PagoInfoResponseDTO obtenerInfo(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        return new PagoInfoResponseDTO(
                pago.getId(),
                pago.getIdUsuario(),
                pago.getEstado(),
                pago.getPaquete().getDuracionMinutos()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PagoDetalleResponseDTO obtenerPagoConCuotas(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        return pagoMapper.toDetalleResponse(pago);
    }

    @Transactional
    @Override
    public PagoListadoResponseDTO crearPagoContado(PagoContadoRequestDTO dto) {

        // El usuario vive en otro servicio: aqui solo guardamos su id (dto.usuarioId()),
        // que el ADMIN indica en la compra. La existencia del usuario se valida via Camel (Fase 6).
        Paquete paquete = paqueteRepository.findById(dto.paqueteId())
                .orElseThrow(() -> new NotFoundException("Paquete no encontrado"));

        Pago pago = pagoMapper.toPagoContado(dto, paquete);

        pago.setTipoPago(TipoPagoEnum.CONTADO);
        pago.setEstado(EstadoPagoEnum.PAGADO);
        pago.setFechaPago(LocalDateTime.now());
        String numeroBoleta = UUID.randomUUID().toString().substring(0, 8);
        pago.setNumeroBoleta(numeroBoleta);

        Pago guardado = pagoRepository.save(pago);

        // FLUJO A (comprar paquete -> cargar horas): notificamos a Usuarios via el bus.
        usuarioSaldoClient.cargarHoras(guardado.getIdUsuario(), paquete.getDuracionMinutos());

        return pagoMapper.toListadoResponse(guardado);
    }

    @Transactional
    @Override
    public PagoDetalleResponseDTO crearPagoCuotas(PagoCuotasRequestDTO dto) {

        // El usuario vive en otro servicio: aqui solo guardamos su id (dto.usuarioId()),
        // que el ADMIN indica en la compra.
        Paquete paquete = paqueteRepository.findById(dto.paqueteId())
                .orElseThrow(() -> new NotFoundException("Paquete no encontrado"));

        validarPrimerPago(dto, paquete);

        Pago pago = pagoMapper.toPagoCuotas(dto, paquete);

        pago.setTipoPago(TipoPagoEnum.CUOTAS);
        pago.setEstado(EstadoPagoEnum.PENDIENTE);
        pago.setMetodoPago(dto.metodoPago());
        pago.setFechaPago(LocalDateTime.now());
        String numeroBoleta = UUID.randomUUID().toString().substring(0, 8);
        pago.setNumeroBoleta(numeroBoleta);

        List<DetallePago> detalles = generarCuotas(dto, paquete, pago);
        pago.setDetalles(detalles);

        Pago guardado = pagoRepository.save(pago);

        // FLUJO A (comprar paquete -> cargar horas): notificamos a Usuarios via el bus.
        usuarioSaldoClient.cargarHoras(guardado.getIdUsuario(), paquete.getDuracionMinutos());

        return pagoMapper.toDetalleResponse(guardado);
    }

    private void validarPrimerPago(PagoCuotasRequestDTO dto, Paquete paquete) {
        if (dto.montoPrimerPago().compareTo(paquete.getPrecioTotal()) >= 0)
            throw new BusinessException("El primer pago no puede ser mayor o igual al total.");
    }

    private List<DetallePago> generarCuotas(PagoCuotasRequestDTO dto, Paquete paquete, Pago pago) {

        List<DetallePago> detalles = new ArrayList<>();

        BigDecimal total = paquete.getPrecioTotal();
        BigDecimal primera = dto.montoPrimerPago();
        BigDecimal restante = total.subtract(primera);
        int cuotasRestantes = dto.cuotas() - 1;

        // Primera cuota pagada
        detalles.add(DetallePago.builder()
                .pago(pago)
                .numeroCuota(1)
                .montoCuota(primera)
                        .metodoPago(dto.metodoPago())
                .fechaVencimiento(LocalDate.now())
                .estadoCuota(EstadoPagoEnum.PAGADO)
                .build()
        );

        // Cuotas restantes
        BigDecimal montoCuotaRestante = restante.divide(
                BigDecimal.valueOf(cuotasRestantes),
                2, RoundingMode.HALF_UP
        );

        for (int i = 2; i <= dto.cuotas(); i++) {
            detalles.add(DetallePago.builder()
                    .pago(pago)
                    .numeroCuota(i)
                    .montoCuota(montoCuotaRestante)
                            .metodoPago(MetodoPagoEnum.PENDIENTE)
                    .fechaVencimiento(LocalDate.now().plusMonths(i - 1))
                    .estadoCuota(EstadoPagoEnum.PENDIENTE)
                    .build()
            );
        }

        return detalles;
    }


    @Transactional
    @Override
    public void pagarCuota(Long idPago, Integer numeroCuota, MetodoPagoEnum metodoPago) {

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        if (pago.getTipoPago() != TipoPagoEnum.CUOTAS) {
            throw new BusinessException("Solo se pueden cancelar cuotas de pagos en modalidad CUOTAS.");
        }

        DetallePago cuota = pago.getDetalles().stream()
                .filter(d -> d.getNumeroCuota().equals(numeroCuota))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe la cuota número " + numeroCuota));

        if (cuota.getEstadoCuota() != EstadoPagoEnum.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar cuotas pendientes.");
        }

        // Método de pago queda registrado tanto en PAGO como en la CUOTA
        pago.setMetodoPago(metodoPago);
        cuota.setMetodoPago(metodoPago);

        // Se marca la cuota como pagada
        cuota.setEstadoCuota(EstadoPagoEnum.PAGADO);

        // Reevaluamos el estado general del pago
        boolean todasPagadas = pago.getDetalles().stream()
                .allMatch(c -> c.getEstadoCuota() == EstadoPagoEnum.PAGADO);

        if (todasPagadas) {
            pago.setEstado(EstadoPagoEnum.PAGADO);
        } else {
            pago.setEstado(EstadoPagoEnum.PENDIENTE);
        }

        pagoRepository.save(pago);
    }

    @Transactional
    @Override
    public void suspenderPago(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));

        if (pago.getTipoPago() != TipoPagoEnum.CUOTAS) {
            throw new BusinessException("Solo se pueden cancelar pagos que fueron realizados en cuotas.");
        }

        if (pago.getEstado() != EstadoPagoEnum.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar pagos pendientes.");
        }

        pago.getDetalles().forEach(detalle -> {
            if (detalle.getEstadoCuota() == EstadoPagoEnum.PENDIENTE) {
                detalle.setEstadoCuota(EstadoPagoEnum.CANCELADO);
            }
        });

        pago.setEstado(EstadoPagoEnum.CANCELADO);
        pagoRepository.save(pago);
    }
}
