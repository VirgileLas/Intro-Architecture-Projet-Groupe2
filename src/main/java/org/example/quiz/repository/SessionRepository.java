package org.example.quiz.repository;

import java.time.Instant;
import java.util.List;

import org.example.quiz.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * REPOSITORY JPA - Interface pour les opérations CRUD sur les sessions.
 * 
 * Spring Data JPA génère automatiquement l'implémentation !
 * 
 * Méthodes personnalisées (Query Methods) :
 * Spring génère la requête SQL à partir du nom de la méthode.
 */
@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    /**
     * Recherche toutes les sessions d'un utilisateur par son username.
     * Génère : SELECT * FROM sessions WHERE username = ?
     */
    List<SessionEntity> findByUsername(String username);

    /**
     * Supprime toutes les sessions d'un utilisateur par son username.
     * Génère : DELETE FROM sessions WHERE username = ?
     */
    void deleteByUsername(String username);

    /**
     * Supprime toutes les sessions expirées.
     * Génère : DELETE FROM sessions WHERE expires_at < ?
     */
    void deleteByExpiresAtBefore(Instant now);
}
