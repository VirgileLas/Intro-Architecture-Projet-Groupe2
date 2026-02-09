package org.example.quiz.service;

import java.util.List;
import java.util.Optional;

import org.example.quiz.model.User;
import org.example.quiz.model.UserEntity;
import org.example.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

/**
 * USER REGISTRY
 * Service qui gère les utilisateurs via JPA Repository.
 * 
 * COUCHE SERVICE : Contient la logique métier
 * - Utilise UserRepository pour les opérations CRUD
 * - Convertit entre UserEntity (persistance) et User (DTO)
 */
@Service
@Transactional
public class UserRegistry {

    private final UserRepository userRepository;

    /**
     * Injection du repository via constructeur.
     */
    @Autowired
    public UserRegistry(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Initialisation avec quelques utilisateurs de test.
     * @PostConstruct s'exécute après l'injection de dépendances.
     */
    @PostConstruct
    public void init() {
        // Évite de recréer si déjà présents
        if (userRepository.count() == 0) {
            // Utilisateurs de démonstration
            register("alice", "password123", "USER");
            register("bob", "secret456", "USER");
            register("admin", "admin", "ADMIN");
            System.out.println(" Utilisateurs de test créés en base H2");
        }
    }

    // ==================== CRUD Operations ====================

    /**
     * CREATE - Enregistre un nouvel utilisateur en base.
     */
    public UserEntity register(String username, String password, String authority) {
        UserEntity entity = new UserEntity(username, password, authority);
        return userRepository.save(entity);
    }

    /**
     * READ - Recherche un utilisateur par son username.
     * Retourne un User (DTO) pour rester compatible avec le code existant.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::toUser);
    }

    /**
     * READ - Récupère l'entité complète (avec ID).
     */
    public Optional<UserEntity> findEntityByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * READ - Liste tous les utilisateurs.
     */
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    /**
     * UPDATE - Met à jour un utilisateur existant.
     */
    public Optional<UserEntity> update(String username, String newPassword, String newAuthority) {
        return userRepository.findByUsername(username)
                .map(entity -> {
                    if (newPassword != null) entity.setPassword(newPassword);
                    if (newAuthority != null) entity.setAuthority(newAuthority);
                    return userRepository.save(entity);
                });
    }

    /**
     * DELETE - Supprime un utilisateur par username.
     */
    public boolean delete(String username) {
        if (userRepository.existsByUsername(username)) {
            userRepository.deleteByUsername(username);
            return true;
        }
        return false;
    }

    // ==================== Méthodes métier ====================

    /**
     * Vérifie les identifiants d'un utilisateur.
     * Utilise la requête JPA findByUsernameAndPassword.
     * @return L'utilisateur si les identifiants sont corrects, sinon Optional.empty()
     */
    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password)
                .map(UserEntity::toUser);
    }

    /**
     * Vérifie si un utilisateur existe.
     */
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Compte le nombre total d'utilisateurs.
     */
    public long count() {
        return userRepository.count();
    }
}
