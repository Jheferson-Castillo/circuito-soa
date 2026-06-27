package com.spring.boot.carro.pagos;

import com.spring.boot.carro.pagos.persistence.entity.Paquete;
import com.spring.boot.carro.pagos.persistence.repository.PaqueteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class ServicioPagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioPagosApplication.class, args);
    }

    // Carga unos paquetes de prueba SOLO si la tabla esta vacia,
    // para poder verificar el GET /api/v1/paquetes tras el primer arranque.
    @Bean
    CommandLineRunner init(PaqueteRepository paqueteRepository) {
        return args -> {
            if (paqueteRepository.count() > 0) {
                return;
            }
            List<Paquete> paquetes = List.of(
                    Paquete.builder()
                            .nombre("PAQUETE + SEGURO")
                            .descripcion("Paquete incluye 10 horas de clase + examen medico")
                            .duracionMinutos(240)
                            .precioTotal(BigDecimal.valueOf(800.00))
                            .activo(true)
                            .build(),
                    Paquete.builder()
                            .nombre("CORPORATIVO")
                            .descripcion("Paquete incluye 20 horas")
                            .duracionMinutos(140)
                            .precioTotal(BigDecimal.valueOf(500.00))
                            .activo(true)
                            .build()
            );
            paqueteRepository.saveAll(paquetes);
        };
    }
}
