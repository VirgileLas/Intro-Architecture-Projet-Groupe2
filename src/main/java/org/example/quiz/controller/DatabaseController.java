package org.example.quiz.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.quiz.model.Session;
import org.example.quiz.model.UserEntity;
import org.example.quiz.service.SessionManager;
import org.example.quiz.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller pour tester et visualiser les données de la base H2
 */
@RestController
@RequestMapping("/api/db")
public class DatabaseController {

    private final UserRegistry userRegistry;
    private final SessionManager sessionManager;

    @Autowired
    public DatabaseController(UserRegistry userRegistry, SessionManager sessionManager) {
        this.userRegistry = userRegistry;
        this.sessionManager = sessionManager;
    }

    /**
     * Vérifie le token Bearer et retourne la session si valide.
     */
    private Optional<Session> extractAndVerifyToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return sessionManager.verify(token);
    }

    /**
     * Liste tous les utilisateurs en base
     * GET http://localhost:8080/api/db/users
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        List<UserEntity> users = userRegistry.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", users.size());
        response.put("users", users.stream()
            .map(u -> Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "authority", u.getAuthority()
            ))
            .collect(Collectors.toList()));
        
        return response;
    }
}
