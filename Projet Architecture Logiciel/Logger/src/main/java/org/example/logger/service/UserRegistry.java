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
        user.setEnabled(true); 
        userRepository.save(user);
    }
    public void register(String username, Credentials credentials, Authority authority) {
        User newUser = new User(username, credentials, authority);
        userRepository.save(newUser);
        String realTokenId = sessionManager.createSession(newUser);
        String email = username + "@example.com"; 
        rabbitMQPublisher.publishUserRegisteredEvent(username, email, realTokenId);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findById(username);
    }
    public Optional<User> authenticate(String username, Credentials inputCredentials) {
        return findByUsername(username)
                .filter(User::isEnabled) 
                .filter(user -> user.credentials().matches(inputCredentials)); 
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
    public void updateUser(User user) {
        userRepository.save(user);
    }
    public java.util.List<User> findAllUsers() {
        return userRepository.findAll();
    }
}