package com.spring.boot.carro.pagos.persistence.repository;

import com.spring.boot.carro.pagos.persistence.entity.Pago;
import com.spring.boot.carro.pagos.presentation.dto.reporte.EstadoPagoDTO;
import com.spring.boot.carro.pagos.presentation.dto.reporte.RentabilidadDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {

    // GRAFICOS ESTADISTICOS

    @Query("SELECT " +
            "p.paquete.nombre, " +
            // MONTO PAGADO: Suma solo lo que realmente entró (PAGADO)
            "CAST(SUM(CASE " +
            "  WHEN p.tipoPago = 'CONTADO' AND p.estado = 'PAGADO' THEN p.monto " +
            "  WHEN p.tipoPago = 'CUOTAS' AND d.estadoCuota = 'PAGADO' THEN d.montoCuota " +
            "  ELSE 0 END) AS double), " +
            // MONTO PENDIENTE: Solo si el padre NO está cancelado y la cuota es pendiente
            "CAST(SUM(CASE " +
            "  WHEN p.estado != 'CANCELADO' AND p.tipoPago = 'CONTADO' AND p.estado = 'PENDIENTE' THEN p.monto " +
            "  WHEN p.estado != 'CANCELADO' AND p.tipoPago = 'CUOTAS' AND d.estadoCuota = 'PENDIENTE' THEN d.montoCuota " +
            "  ELSE 0 END) AS double), " +
            // MONTO CANCELADO: Suma lo que se marcó como cancelado en detalle o si el padre se canceló siendo contado
            "CAST(SUM(CASE " +
            "  WHEN p.estado = 'CANCELADO' AND p.tipoPago = 'CONTADO' THEN p.monto " +
            "  WHEN p.tipoPago = 'CUOTAS' AND d.estadoCuota = 'CANCELADO' THEN d.montoCuota " +
            "  ELSE 0 END) AS double), " +
            // TOTAL CLIENTES: Conteo de boletas únicas
            "COUNT(DISTINCT p.id) " +
            "FROM Pago p " +
            "LEFT JOIN p.detalles d " +
            "WHERE EXTRACT(MONTH FROM p.fechaPago) = :mes AND EXTRACT(YEAR FROM p.fechaPago) = :anio " +
            "GROUP BY p.paquete.nombre")
    List<RentabilidadDTO> obtenerRentabilidadReal(@Param("mes") int mes, @Param("anio") int anio);

    @Query("SELECT " +
            "CAST(p.estado AS string), " + // Convertimos el Enum a String para el DTO
            // Antes COUNT(DISTINCT p.usuario.id); ahora el usuario es solo un id (sin relacion JPA).
            "COUNT(DISTINCT p.idUsuario) " +
            "FROM Pago p " +
            "WHERE EXTRACT(MONTH FROM p.fechaPago) = :mes AND EXTRACT(YEAR FROM p.fechaPago) = :anio " +
            "GROUP BY p.estado")
    List<EstadoPagoDTO> obtenerResumenClientesPorEstado(@Param("mes") int mes, @Param("anio") int anio);

    @Query("SELECT p.id, " +
            "SUM(CASE WHEN d.estadoCuota = 'PENDIENTE' AND d.fechaVencimiento < CURRENT_DATE THEN 1 ELSE 0 END), " +
            "(CURRENT_DATE - p.fechaPago) " +
            "FROM Pago p JOIN p.detalles d " +
            "GROUP BY p.id, p.fechaPago")
    List<Object[]> obtenerDatosCrudosTotales();
}
