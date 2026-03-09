package org.example.logger.component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange:auth.events}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key:auth.user-registered}")
    private String routingKey;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserRegisteredEvent(String userId, String email, String tokenId) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            // Construction de la structure de données data
            Map<String, String> data = new HashMap<>();
            data.put("userId", userId);
            data.put("email", email);
            data.put("tokenId", tokenId);

            // Construction de la payload JSON selon la structure requise
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "UserRegistered");
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("occurredAt", Instant.now().toString());
            payload.put("data", data);

            // Conversion en JSON dynamique
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Publication avec ajout du header x-correlation-id
            rabbitTemplate.convertAndSend(exchange, routingKey, jsonPayload, message -> {
                MessageProperties properties = message.getMessageProperties();
                properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                properties.setHeader("x-correlation-id", correlationId);
                return message;
            });

        } catch (Exception e) {
            // Loggez l'erreur sans bloquer le processus d'inscription
            System.err.println("Erreur lors de la publication vers RabbitMQ : " + e.getMessage());
        }
    }
}
