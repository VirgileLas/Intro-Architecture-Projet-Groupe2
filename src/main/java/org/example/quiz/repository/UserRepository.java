package org.example.quiz.repository;

import java.util.Optional;

import org.example.quiz.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * REPOSITORY JPA - Interface pour les opérations CRUD sur les utilisateurs.
 * 
 * Spring Data JPA génère automatiquement l'implémentation !
 * 
 * Hérite de JpaRepository qui fournit :
 * - save(entity)      : CREATE/UPDATE
 * - findById(id)      : READ par ID
 * - findAll()         : READ tous
 * - deleteById(id)    : DELETE par ID
 * - count()           : Compter les entités
 * - existsById(id)    : Vérifier existence
 * 
 * Méthodes personnalisées (Query Methods) :
 * Spring génère la requête SQL à partir du nom de la méthode !
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Recherche un utilisateur par son username.
     * Génère : SELECT * FROM users WHERE username = ?
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Vérifie si un username existe déjà.
     * Génère : SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);

    /**
     * Recherche un utilisateur par username ET password.
     * Génère : SELECT * FROM users WHERE username = ? AND password = ?
     */
    Optional<UserEntity> findByUsernameAndPassword(String username, String password);

    /**
     * Supprime un utilisateur par son username.
     * Génère : DELETE FROM users WHERE username = ?
     */
    void deleteByUsername(String username);
}
