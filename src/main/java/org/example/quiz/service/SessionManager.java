package org.example.quiz.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.example.quiz.model.Session;
import org.example.quiz.model.User;
import org.springframework.stereotype.Service;

/**
 * SESSION MANAGER
 * Service Singleton qui gère les tokens actifs (Token + TTL + UID).
 * HashMap pour accès rapide par token.
 */
@Service
public class SessionManager {

    // Durée de vie par défaut d'une session (30 minutes)
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    // Stockage des sessions actives (token -> Session)
    private final Map<String, Session> sessions = new HashMap<>();

    /**
     * Crée une nouvelle session pour un utilisateur.
     * @return Le token généré
     */
    public String createSession(User user) {
        // Génération d'un token unique (UUID)
        String token = UUID.randomUUID().toString();
        
        // Calcul de la date d'expiration
        Instant expiresAt = Instant.now().plus(DEFAULT_TTL);
        
        // Création et stockage de la session
        Session session = new Session(token, user.username(), expiresAt);
        sessions.put(token, session);
        
        return token;
    }

    /**
     * Vérifie un token et retourne la session si elle est valide.
     */
    public Optional<Session> verify(String token) {
        Session session = sessions.get(token);
        
        if (session == null) {
            return Optional.empty();
        }
        
        // Vérifie si la session n'a pas expiré
        if (!session.isValid()) {
            // Nettoyage automatique des sessions expirées
            sessions.remove(token);
            return Optional.empty();
        }
        
        return Optional.of(session);
    }

    /**
     * Invalide une session (logout).
     */
    public void invalidate(String token) {
        sessions.remove(token);
    }

    /**
     * Retourne le nombre de sessions actives.
     */
    public int getActiveSessionCount() {
        // Nettoie les sessions expirées et retourne le compte
        sessions.entrySet().removeIf(entry -> !entry.getValue().isValid());
        return sessions.size();
    }
}
