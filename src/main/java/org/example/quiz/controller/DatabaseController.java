package org.example.quiz.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.quiz.model.UserEntity;
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

    @Autowired
    public DatabaseController(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
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

    /**
     * Statistiques de la base
     * GET http://localhost:8080/api/db/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRegistry.count());
        stats.put("database", "H2 (in-memory)");
        stats.put("url", "jdbc:h2:mem:quizdb");
        stats.put("status", "✅ Connected");
        
        return stats;
    }
}
