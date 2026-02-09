package org.example.quiz.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.example.quiz.model.Session;
import org.example.quiz.model.SessionEntity;
import org.example.quiz.model.User;
import org.example.quiz.model.UserEntity;
import org.example.quiz.repository.SessionRepository;
import org.example.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * SESSION MANAGER
 * Service Singleton qui gère les tokens actifs (Token + TTL + UID).
 * 
 * AVANT  : HashMap en mémoire (perdu au redémarrage)
 * APRÈS  : Persisté en base H2 via SessionRepository
 * 
 * Les sessions sont liées aux utilisateurs via @ManyToOne.
 * → Quand un utilisateur est supprimé, ses sessions sont supprimées automatiquement (cascade).
 */
@Service
@Transactional
public class SessionManager {

    // Durée de vie par défaut d'une session (30 minutes)
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SessionManager(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crée une nouvelle session pour un utilisateur et la persiste en base.
     * @return Le token généré
     */
    public String createSession(User user) {
        // Génération d'un token unique (UUID)
        String token = UUID.randomUUID().toString();
        
        // Calcul de la date d'expiration
        Instant expiresAt = Instant.now().plus(DEFAULT_TTL);
        
        // Récupère l'entité utilisateur depuis la base
        UserEntity userEntity = userRepository.findByUsername(user.username())
                .orElseThrow(() -> new RuntimeException("User not found: " + user.username()));
        
        // Création et persistance de la session
        SessionEntity sessionEntity = new SessionEntity(token, userEntity, user.username(), expiresAt);
        sessionRepository.save(sessionEntity);
        
        return token;
    }

    /**
     * Vérifie un token et retourne la session si elle est valide.
     */
    public Optional<Session> verify(String token) {
        Optional<SessionEntity> entity = sessionRepository.findById(token);
        
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        
        SessionEntity sessionEntity = entity.get();
        
        // Vérifie si la session n'a pas expiré
        if (!sessionEntity.isValid()) {
            // Nettoyage automatique des sessions expirées
            sessionRepository.deleteById(token);
            return Optional.empty();
        }
        
        return Optional.of(sessionEntity.toSession());
    }

    /**
     * Invalide une session (logout) — supprime de la base.
     */
    public void invalidate(String token) {
        sessionRepository.deleteById(token);
    }

    /**
     * Retourne le nombre de sessions actives (non expirées).
     */
    public int getActiveSessionCount() {
        // Nettoie les sessions expirées en base
        sessionRepository.deleteByExpiresAtBefore(Instant.now());
        return (int) sessionRepository.count();
    }
}
