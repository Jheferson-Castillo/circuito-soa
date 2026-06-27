package com.spring.boot.carro.reservas;

import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.reservas.persistence.enums.TipoTransmisionEnum;
import com.spring.boot.carro.reservas.persistence.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ServicioReservasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioReservasApplication.class, args);
    }

    // Carga unos vehiculos de prueba SOLO si la tabla esta vacia,
    // para poder verificar el GET /api/v1/vehiculos tras el primer arranque.
    @Bean
    CommandLineRunner init(VehiculoRepository vehiculoRepository) {
        return args -> {
            if (vehiculoRepository.count() > 0) {
                return;
            }
            List<Vehiculo> vehiculos = List.of(
                    Vehiculo.builder()
                            .placa("ABC-456").marca("Audi").modelo("Q5 Sportback")
                            .tipoTransmision(TipoTransmisionEnum.AUTOMATICO)
                            .estado(EstadoVehiculosEnum.DISPONIBLE).activo(true)
                            .build(),
                    Vehiculo.builder()
                            .placa("CXG-328").marca("BMW").modelo("Serie 5")
                            .tipoTransmision(TipoTransmisionEnum.MANUAL)
                            .estado(EstadoVehiculosEnum.DISPONIBLE).activo(true)
                            .build(),
                    Vehiculo.builder()
                            .placa("S2R-571").marca("Changan").modelo("CS15")
                            .tipoTransmision(TipoTransmisionEnum.MANUAL)
                            .estado(EstadoVehiculosEnum.DISPONIBLE).activo(true)
                            .build()
            );
            vehiculoRepository.saveAll(vehiculos);
        };
    }
}
