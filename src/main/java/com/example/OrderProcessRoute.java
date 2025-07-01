package com.example;

import org.apache.camel.builder.RouteBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderProcessRoute extends RouteBuilder {

    private final Map<String, String> orderStates = new HashMap<>();

    @Override
    public void configure() {

        // Crea o "recibe" un pedido cada 5 segundos
        from("timer:nueva-orden?period=5000")
            .routeId("nueva-orden") // crea la ruta "nueva-orden"
            .process(exchange -> {
                String ordenId = UUID.randomUUID().toString(); // Genera el ID 
                exchange.getMessage().setHeader("ordenId", ordenId);
                orderStates.put(ordenId, "CREADA");
                exchange.getMessage().setBody("Orden recibida: " + ordenId);
            })
            .log("${body}")
            .to("direct:validacion-inventario"); // manda a la ruta de validación de inventario

        // Paso 2: validar inventario
        from("direct:validacion-inventario") // llega el mensaje para ser validado
            .process(exchange -> {
                String ordenId = exchange.getMessage().getHeader("ordenId", String.class);
                orderStates.put(ordenId, "VALIDADO"); // cambia su estado
                exchange.getMessage().setBody("Inventario validado para la orden: " + ordenId);
            })
            .log("${body}")
            .to("direct:proceso-pago"); // manda a la ruta de procesamiento de pago

        // Paso 3: procesar pago
        from("direct:proceso-pago")
            .process(exchange -> {
                String ordenId = exchange.getMessage().getHeader("ordenId", String.class);
                boolean pagoExitoso = Math.random() > 0.3; // 70% chance de éxito
                if (pagoExitoso) {
                    orderStates.put(ordenId, "PAGADO");
                    exchange.getMessage().setBody("Pago procesado para la orden: " + ordenId);
                    exchange.getMessage().setHeader("estadoPago", "PAGADO");
                } else {
                    orderStates.put(ordenId, "RECHAZADO");
                    exchange.getMessage().setBody("Pago rechazado para la orden: " + ordenId);
                    exchange.getMessage().setHeader("estadoPago", "RECHAZADO");
                }
            })
            .log("${body}")
            .choice()
                .when(header("estadoPago").isEqualTo("PAGADO")) //cuando el header es PAGADO manda la ruta de confirmados
                    .to("direct:orden-confirmada")
                .otherwise()
                    .to("direct:orden-rechazada"); // osino a la cola de rechazados

        // Paso 4a: confirmar pedido
        from("direct:orden-confirmada")
            .process(exchange -> {
                String ordenId = exchange.getMessage().getHeader("ordenId", String.class);
                orderStates.put(ordenId, "CONFIRMADA");
                exchange.getMessage().setBody("Orden confirmada: " + ordenId);
            })
            .log("${body}");

        // Paso 4b: rechazar pedido
        from("direct:orden-rechazada")
            .process(exchange -> {
                String ordenId = exchange.getMessage().getHeader("ordenId", String.class);
                orderStates.put(ordenId, "RECHAZADA");
                exchange.getMessage().setBody("Orden rechazada: " + ordenId);
            })
            .log("${body}");
    }
}
