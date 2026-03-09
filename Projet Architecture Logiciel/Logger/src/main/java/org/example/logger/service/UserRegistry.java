package org.example.logger.service;

import java.util.Optional;

import org.example.logger.component.RabbitMQPublisher;
import org.example.logger.model.Authority;
import org.example.logger.model.Credentials;
import org.example.logger.model.User;
import org.example.logger.model.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRegistry {

    private final UserRepository userRepository;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final SessionManager sessionManager;

    public UserRegistry(UserRepository userRepository, RabbitMQPublisher rabbitMQPublisher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.rabbitMQPublisher = rabbitMQPublisher;
        this.sessionManager = sessionManager;
    }

    public void registerInitUser(String username, Credentials credentials, Authority authority) {
        User user = new User(username, credentials, authority);
        user.setEnabled(true); // Utilisateurs par défaut sont déjà activés
        userRepository.save(user);
    }

    /**
     * Enregistre un nouvel utilisateur avec des types forts.
     */
    public void register(String username, Credentials credentials, Authority authority) {
        User newUser = new User(username, credentials, authority);
        userRepository.save(newUser);
        
        // Création d'une VRAIE session pour que le lien de vérification fonctionne
        String realTokenId = sessionManager.createSession(newUser);
        
        // Publication de l'événement RabbitMQ après l'enregistrement
        String email = username + "@example.com"; // Génération d'un email basé sur le username pour l'exemple
        rabbitMQPublisher.publishUserRegisteredEvent(username, email, realTokenId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findById(username);
    }

    /**
     * Authentifie en comparant des objets Credentials (et s'il est activé).
     */
    public Optional<User> authenticate(String username, Credentials inputCredentials) {
        return findByUsername(username)
                .filter(User::isEnabled) // NOUVEAU: On vérifie que le compte est activé
                .filter(user -> user.credentials().matches(inputCredentials)); 
                // La logique de comparaison est déléguée à l'objet Credentials
    }

    public boolean exists(String username) {
        return userRepository.existsById(username);
    }
    
    public void enableUser(String username) {
        userRepository.findById(username).ifPresent(user -> {
            user.setEnabled(true);
            userRepository.save(user);
        });
    }

    // Endpoint outil permettant de consulter toute la DB d'utilisateurs
    public java.util.List<User> findAllUsers() {
        return userRepository.findAll();
    }
}