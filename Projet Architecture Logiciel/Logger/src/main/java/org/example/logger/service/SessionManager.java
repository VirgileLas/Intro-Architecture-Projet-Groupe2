package org.example.logger.service;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.example.logger.model.Session;
import org.example.logger.model.User;
import org.springframework.stereotype.Service;
@Service
public class SessionManager {
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private final Map<String, Session> sessions = new HashMap<>();
    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(DEFAULT_TTL);
        Session session = new Session(token, user.username(), expiresAt);
        sessions.put(token, session);
        return token;
    }
    public Optional<Session> verify(String token) {
        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (!session.isValid()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }
    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
    public int getActiveSessionCount() {
        sessions.entrySet().removeIf(entry -> !entry.getValue().isValid());
        return sessions.size();
    }
}
