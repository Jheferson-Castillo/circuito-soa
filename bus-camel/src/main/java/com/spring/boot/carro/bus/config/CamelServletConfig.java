package com.spring.boot.carro.bus.config;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra manualmente el servlet de Camel mapeado a /* (todas las rutas),
 * para que el bus sea la unica puerta de entrada.
 *
 * Se hace a mano (en vez de usar camel.servlet.mapping.context-path) porque el
 * auto-config ServletMappingAutoConfiguration de camel-servlet 4.10.3 es
 * incompatible con Spring Boot 3.5.15 (MultipartProperties). Por eso ademas se
 * excluye ese auto-config en application.properties.
 */
@Configuration
public class CamelServletConfig {

    @Bean
    public ServletRegistrationBean<CamelHttpTransportServlet> camelServletRegistration() {
        ServletRegistrationBean<CamelHttpTransportServlet> registration =
                new ServletRegistrationBean<>(new CamelHttpTransportServlet(), "/*");
        // El nombre debe coincidir con el servletName por defecto del componente "servlet:" de Camel.
        registration.setName("CamelServlet");
        return registration;
    }
}
