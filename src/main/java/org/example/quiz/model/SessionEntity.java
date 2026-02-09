package org.example.quiz.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ENTITÉ JPA - Représente une session persistée en base de données.
 * 
 * Liée à UserEntity via @ManyToOne :
 * - Un utilisateur peut avoir plusieurs sessions actives
 * - La suppression d'un utilisateur supprime ses sessions (cascade côté UserEntity)
 * 
 * Le token sert de clé primaire (pas d'auto-incrémentation).
 */
@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    @Column(length = 36)
    private String token;

    /**
     * Relation ManyToOne vers l'utilisateur propriétaire de la session.
     * FetchType.LAZY : l'utilisateur n'est chargé que si on y accède.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant expiresAt;

    // ==================== Constructeurs ====================

    /**
     * Constructeur par défaut requis par JPA.
     */
    public SessionEntity() {
    }

    /**
     * Constructeur complet.
     */
    public SessionEntity(String token, UserEntity user, String username, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.username = username;
        this.expiresAt = expiresAt;
    }

    // ==================== Getters & Setters ====================

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    // ==================== Méthodes utilitaires ====================

    /**
     * Vérifie si la session est encore valide (non expirée).
     */
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }

    /**
     * Convertit l'entité en record Session (DTO immuable).
     */
    public Session toSession() {
        return new Session(token, username, expiresAt);
    }

    @Override
    public String toString() {
        return "SessionEntity{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
