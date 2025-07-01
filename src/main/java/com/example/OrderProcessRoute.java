package com.example;

import org.apache.camel.builder.RouteBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderProcessRoute extends RouteBuilder {

    private final Map<String, String> orderStates = new HashMap<>();

    @Override
    public void configure() {

        // Paso 1: recibir pedidos (simulados con un timer)
        from("timer:new-order?period=5000")
            .routeId("new-order")
            .process(exchange -> {
                String orderId = UUID.randomUUID().toString();
                exchange.getMessage().setHeader("orderId", orderId);
                orderStates.put(orderId, "CREATED");
                exchange.getMessage().setBody("Order received: " + orderId);
            })
            .log("${body}")
            .to("direct:validate-inventory");

        // Paso 2: validar inventario
        from("direct:validate-inventory")
            .process(exchange -> {
                String orderId = exchange.getMessage().getHeader("orderId", String.class);
                orderStates.put(orderId, "INVENTORY_VALIDATED");
                exchange.getMessage().setBody("Inventory validated for order " + orderId);
            })
            .log("${body}")
            .to("direct:process-payment");

        // Paso 3: procesar pago
        from("direct:process-payment")
            .process(exchange -> {
                String orderId = exchange.getMessage().getHeader("orderId", String.class);
                boolean paymentSuccess = Math.random() > 0.3; // 70% chance de éxito
                if (paymentSuccess) {
                    orderStates.put(orderId, "PAID");
                    exchange.getMessage().setBody("Payment processed for order " + orderId);
                    exchange.getMessage().setHeader("paymentStatus", "SUCCESS");
                } else {
                    orderStates.put(orderId, "PAYMENT_FAILED");
                    exchange.getMessage().setBody("Payment failed for order " + orderId);
                    exchange.getMessage().setHeader("paymentStatus", "FAILURE");
                }
            })
            .log("${body}")
            .choice()
                .when(header("paymentStatus").isEqualTo("SUCCESS"))
                    .to("direct:confirm-order")
                .otherwise()
                    .to("direct:reject-order");

        // Paso 4a: confirmar pedido
        from("direct:confirm-order")
            .process(exchange -> {
                String orderId = exchange.getMessage().getHeader("orderId", String.class);
                orderStates.put(orderId, "CONFIRMED");
                exchange.getMessage().setBody("Order confirmed: " + orderId);
            })
            .log("${body}");

        // Paso 4b: rechazar pedido
        from("direct:reject-order")
            .process(exchange -> {
                String orderId = exchange.getMessage().getHeader("orderId", String.class);
                orderStates.put(orderId, "REJECTED");
                exchange.getMessage().setBody("Order rejected: " + orderId);
            })
            .log("${body}");
    }
}
