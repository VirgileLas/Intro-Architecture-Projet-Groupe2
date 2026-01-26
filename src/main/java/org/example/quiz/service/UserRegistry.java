package org.example.quiz.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.example.quiz.model.User;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * USER REGISTRY
 * Service Singleton qui stocke les utilisateurs (Identity + Authority).
 * HashMap pour accès rapide par username.
 */
@Service
public class UserRegistry {

    // Stockage en mémoire des utilisateurs (username -> User)
    private final Map<String, User> users = new HashMap<>();

    /**
     * Initialisation avec quelques utilisateurs de test.
     * @PostConstruct s'exécute après l'injection de dépendances.
     */
    @PostConstruct
    public void init() {
        // Utilisateurs de démonstration
        register("alice", "password123", "USER");
        register("bob", "secret456", "USER");
        register("admin", "admin", "ADMIN");
    }

    /**
     * Enregistre un nouvel utilisateur.
     */
    public void register(String username, String password, String authority) {
        users.put(username, new User(username, password, authority));
    }

    /**
     * Recherche un utilisateur par son username.
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    /**
     * Vérifie les identifiants d'un utilisateur.
     * @return L'utilisateur si les identifiants sont corrects, sinon Optional.empty()
     */
    public Optional<User> authenticate(String username, String password) {
        return findByUsername(username)
                .filter(user -> user.password().equals(password));
    }

    /**
     * Vérifie si un utilisateur existe.
     */
    public boolean exists(String username) {
        return users.containsKey(username);
    }
}
