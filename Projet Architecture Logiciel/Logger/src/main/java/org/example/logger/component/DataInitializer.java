package org.example.logger.component;

import org.example.logger.model.Authority;
import org.example.logger.model.Credentials;
import org.example.logger.model.UserRepository;
import org.example.logger.service.UserRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRegistry userRegistry;

    public DataInitializer(UserRepository userRepository, UserRegistry userRegistry) {
        this.userRepository = userRepository;
        this.userRegistry = userRegistry;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRegistry.registerInitUser("alice", new Credentials("password123"), new Authority("USER"));
            userRegistry.registerInitUser("bob", new Credentials("secret456"), new Authority("USER"));
            userRegistry.registerInitUser("admin", new Credentials("admin"), new Authority("ADMIN"));
        }
    }
}
