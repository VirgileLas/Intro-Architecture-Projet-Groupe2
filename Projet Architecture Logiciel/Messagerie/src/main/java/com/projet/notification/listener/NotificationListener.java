package com.projet.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.projet.notification.config.RabbitMQConfig;
import com.projet.notification.dto.UserRegisteredEvent;
import com.projet.notification.service.EmailService;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeUserRegisteredEvent(
            UserRegisteredEvent event,
            @Header(name = "x-correlation-id", required = false) String correlationId) {
        
        log.info("[CorrelationID: {}] Event {} received for user {}", correlationId, event.eventId(), event.data() != null ? event.data().userId() : "null");

        try {
            if (event.data() != null) {
                log.info("Envoi de l'email de vérification à : {}", event.data().email());
                emailService.sendVerificationEmail(event.data().email(), event.data().tokenId());
                log.info("Email envoyé avec succès.");
            } else {
                log.warn("Payload 'data' manquant dans l'événement.");
            }
            
        } catch (Exception e) {
            log.error("[CorrelationID: {}] Erreur lors de l'envoi de l'email. Redirection vers la DLQ.", correlationId, e);
            // On lève cette exception spécifique pour s'assurer que le message soit envoyé à la DLQ 
            // plutôt que de bloquer indéfiniment la queue principale si une erreur survenait.
            throw new AmqpRejectAndDontRequeueException("Impossible de notifier l'utilisateur, message transféré vers DLQ", e);
        }
    }
}