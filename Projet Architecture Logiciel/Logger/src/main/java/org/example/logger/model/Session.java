package org.example.logger.model;

import java.time.Instant;

/**
 * Représente une session active avec son token, TTL et UID.
 * Record Java 17+ : immuable et concis.
 */
public record Session(
    String token,
    String username,
    Instant expiresAt
) {
    /**
     * Vérifie si la session est encore valide (non expirée).
     */
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }
}
