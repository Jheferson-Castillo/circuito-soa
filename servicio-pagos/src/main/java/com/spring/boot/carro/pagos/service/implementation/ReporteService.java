package com.spring.boot.carro.pagos.service.implementation;

import com.spring.boot.carro.pagos.persistence.repository.PagoRepository;
import com.spring.boot.carro.pagos.presentation.dto.reporte.AnalisisRetencionDTO;
import com.spring.boot.carro.pagos.presentation.dto.reporte.RentabilidadDTO;
import com.spring.boot.carro.pagos.service.interfaces.IReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService implements IReporteService {

    @Autowired
    private PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<AnalisisRetencionDTO> prepararRetencionHistorica() {
        // Llamamos al nuevo método sin parámetros
        List<Object[]> resultados = pagoRepository.obtenerDatosCrudosTotales();

        // La lógica de negocio se mantiene igual, agrupando todos los registros
        Map<String, List<Integer>> grupos = resultados.stream().collect(Collectors.groupingBy(reg -> {
            long cuotasVencidas = ((Number) reg[1]).longValue();
            int diasDesdePago = ((Number) reg[2]).intValue();

            if (cuotasVencidas > 0 && diasDesdePago > 30) return "ABANDONO";
            if (cuotasVencidas > 0) return "EN RIESGO";
            return "AL DÍA";
        }, Collectors.mapping(reg -> ((Number) reg[2]).intValue(), Collectors.toList())));

        return grupos.entrySet().stream().map(entry -> {
            String categoria = entry.getKey();
            List<Integer> listaDias = entry.getValue();

            long cantidad = listaDias.size();
            double promedio = listaDias.stream().mapToInt(d -> d).average().orElse(0.0);
            double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

            return new AnalisisRetencionDTO(categoria, cantidad, promedioRedondeado);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RentabilidadDTO> calcularRentabilidadMes(int mes, int anio) {
        List<RentabilidadDTO> datos = pagoRepository.obtenerRentabilidadReal(mes, anio);

        return datos.stream().map(d -> {
            double pagado = d.montoTotal() != null ? d.montoTotal() : 0.0;
            double pendiente = d.montoPendiente() != null ? d.montoPendiente() : 0.0;
            double cancelado = d.montoCancelado() != null ? d.montoCancelado() : 0.0;

            return new RentabilidadDTO(
                    d.etiqueta(),
                    Math.round(pagado * 100.0) / 100.0,
                    Math.round(pendiente * 100.0) / 100.0,
                    Math.round(cancelado*100.0)/100.0,
                    d.totalClientes()
            );
        }).collect(Collectors.toList());
    }
}
