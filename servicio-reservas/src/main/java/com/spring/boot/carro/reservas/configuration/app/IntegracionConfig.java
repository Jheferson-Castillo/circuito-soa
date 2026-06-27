package com.spring.boot.carro.reservas.configuration.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient apuntando al bus ESB (puerto 8080). Las llamadas inter-servicio de
 * Reservas (a Pagos y a Usuarios) salen por el bus, que las enruta al destino.
 */
@Configuration
public class IntegracionConfig {

    @Bean
    public RestClient busRestClient(@Value("${services.gateway-url:http://localhost:8080}") String gatewayUrl) {
        return RestClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }
}
