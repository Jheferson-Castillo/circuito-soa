package com.spring.boot.carro.bus.route;

import com.spring.boot.carro.bus.security.JwtAuthProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Bus ESB (Apache Camel) - puerta de entrada unica del sistema (puerto 8080).
 *
 * 1) FILTRO JWT (antes de enrutar): valida el token Bearer con el secreto compartido HS256.
 *    Token ausente/invalido/manipulado/expirado -> 401 directo desde el bus (no se reenvia).
 *    Rutas publicas (login, swagger, /info, /saldo) pasan sin validar.
 * 2) ENRUTADOR CONTENT-BASED: reenvia por HTTP al microservicio correcto (transparente,
 *    conservando metodo, path, query, body y la cabecera Authorization).
 *
 *   /api/v1/usuarios|auth                 -> localhost:8081 (servicio-usuarios)
 *   /api/v1/pagos|paquetes|reportes       -> localhost:8082 (servicio-pagos)
 *   /api/reportes                         -> localhost:8082
 *   /api/v1/reservas|vehiculos            -> localhost:8083 (servicio-reservas)
 */
@Component
public class GatewayRoute extends RouteBuilder {

    private static final String SVC_USUARIOS = "http://localhost:8081";
    private static final String SVC_PAGOS    = "http://localhost:8082";
    private static final String SVC_RESERVAS = "http://localhost:8083";

    private final JwtAuthProcessor jwtAuthProcessor;

    public GatewayRoute(JwtAuthProcessor jwtAuthProcessor) {
        this.jwtAuthProcessor = jwtAuthProcessor;
    }

    @Override
    public void configure() {

        // Si el servicio destino esta caido o no responde, devolvemos 502 indicando a donde se intento enrutar.
        onException(Exception.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setBody(simple("{\"error\":\"Bus ESB: no se pudo contactar al servicio destino\",\"destino\":\"${header.backendUri}\",\"path\":\"${header.fwdPath}\"}"));

        from("servlet:///?matchOnUriPrefix=true")
            .routeId("bus-esb-gateway")
            // Path normalizado con barra inicial.
            .setHeader("fwdPath", simple("/${header.CamelHttpPath}"))

            // -------- 1) FILTRO JWT (antes de enrutar) --------
            .process(jwtAuthProcessor)

            // -------- 2) DECISION: rechazar o enrutar --------
            .choice()
                // Token ausente/invalido: el filtro ya preparo el 401; detenemos sin reenviar.
                .when(simple("${header.busAuth} == 'REJECTED'"))
                    .stop()
                // Enrutador content-based (segun el prefijo del path)
                .when(simple("${header.fwdPath} startsWith '/api/v1/usuarios'"))
                    .setHeader("backendUri", constant(SVC_USUARIOS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/auth'"))
                    .setHeader("backendUri", constant(SVC_USUARIOS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/pagos'"))
                    .setHeader("backendUri", constant(SVC_PAGOS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/paquetes'"))
                    .setHeader("backendUri", constant(SVC_PAGOS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/reportes'"))
                    .setHeader("backendUri", constant(SVC_PAGOS))
                .when(simple("${header.fwdPath} startsWith '/api/reportes'"))
                    .setHeader("backendUri", constant(SVC_PAGOS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/reservas'"))
                    .setHeader("backendUri", constant(SVC_RESERVAS))
                .when(simple("${header.fwdPath} startsWith '/api/v1/vehiculos'"))
                    .setHeader("backendUri", constant(SVC_RESERVAS))
                .otherwise()
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .setBody(simple("{\"error\":\"El bus ESB no tiene ruta para el path: ${header.fwdPath}\"}"))
                    .stop()
            .end()
            // Quitamos HTTP_PATH para que bridgeEndpoint NO vuelva a anexar el path relativo;
            // el path completo va explicito en la URI de toD.
            .removeHeader(Exchange.HTTP_PATH)
            .log("ESB -> ${header.CamelHttpMethod} ${header.fwdPath} => ${header.backendUri}")
            // Reenvio transparente al servicio destino. bridgeEndpoint conserva metodo/query/body/Authorization.
            .toD("${header.backendUri}${header.fwdPath}?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }
}
