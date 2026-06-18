package com.lpzcahuillan.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class NotificationConsumer {

    @RabbitListener(queues = "notification.queue")
    public void consumeMessage(Map<String, Object> event) {
        log.info("==== NUEVA NOTIFICACIÓN RECIBIDA ====");
        log.info("Tipo de Evento: {}", event.get("eventType"));
        log.info("Mensaje: {}", event.get("message"));
        log.info("Detalles: {}", event.get("details"));
        log.info("======================================");
    }
}
