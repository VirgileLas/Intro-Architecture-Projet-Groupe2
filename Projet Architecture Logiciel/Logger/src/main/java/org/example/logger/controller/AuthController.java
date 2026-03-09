package org.example.logger.controller;

import java.util.Optional;

import org.example.logger.model.Authority;
import org.example.logger.model.Credentials;
import org.example.logger.model.Session;
import org.example.logger.model.User;
import org.example.logger.service.SessionManager;
import org.example.logger.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRegistry userRegistry;
    private final SessionManager sessionManager;

    @Autowired
    public AuthController(UserRegistry userRegistry, SessionManager sessionManager) {
        this.userRegistry = userRegistry;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestParam String username, @RequestParam String password) {
        // Transformation des données brutes en objets métiers
        Credentials inputCredentials = new Credentials(password);

        Optional<User> user = userRegistry.authenticate(username, inputCredentials);
        
        if (user.isEmpty()) {
            return new LoginResponse(false, null, null, "Invalid credentials");
        }
        
        String token = sessionManager.createSession(user.get());
        // On récupère le nom de l'autorité pour l'envoyer au client (String)
        return new LoginResponse(true, token, user.get().authority().getName(), null);
    }

    @GetMapping("/verify")
    public VerifyResponse verify(@RequestParam String token) {
        Optional<Session> session = sessionManager.verify(token);
        
        if (session.isEmpty()) {
            return new VerifyResponse(false, null, null, "Invalid or expired token");
        }

        String username = session.get().username();
        
        // On active le compte utilisateur après avoir vérifié le token de son mail
        userRegistry.enableUser(username);
        
        String authority = userRegistry.findByUsername(username)
                .map(u -> u.authority().getName()) // Conversion Authority -> String
                .orElse("UNKNOWN");
        
        return new VerifyResponse(true, username, authority, null);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@RequestParam String token) {
        sessionManager.invalidate(token);
        return new LogoutResponse(true, "Session terminated");
    }

    @GetMapping("/validate")
    public org.springframework.http.ResponseEntity<?> validateSession(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        
        if (session.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(401).body("Invalid or expired session");
        }
        
        String username = session.get().username();
        Optional<User> user = userRegistry.findByUsername(username);
        
        if (user.isEmpty() || !user.get().isEnabled()) {
            return org.springframework.http.ResponseEntity.status(403).body("User not enabled or not found");
        }
        
        return org.springframework.http.ResponseEntity.ok("Authorized");
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestParam String username, @RequestParam String password) {
        if (userRegistry.exists(username)) {
            return new RegisterResponse(false, "Username already exists");
        }
        
        // Création des objets métiers
        Credentials creds = new Credentials(password);
        Authority auth = new Authority("USER");

        userRegistry.register(username, creds, auth);
        return new RegisterResponse(true, "User registered successfully");
    }

    // NOUVEL ENDPOINT : Permet d'afficher toute la DB en JSON pour le debug
    @GetMapping("/users")
    public java.util.List<User> getAllUsers() {
        return userRegistry.findAllUsers();
    }

    public record LoginResponse(boolean success, String token, String authority, String error) {}
    public record VerifyResponse(boolean valid, String username, String authority, String error) {}
    public record LogoutResponse(boolean success, String message) {}
    public record RegisterResponse(boolean success, String message) {}
}