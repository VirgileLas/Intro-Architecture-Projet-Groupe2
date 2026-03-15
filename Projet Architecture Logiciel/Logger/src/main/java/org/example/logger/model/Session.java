package org.example.logger.model;
import java.time.Instant;
public record Session(
    String token,
    String username,
    Instant expiresAt
) {
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }
}
