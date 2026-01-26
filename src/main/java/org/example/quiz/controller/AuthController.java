package org.example.quiz.controller;

import java.util.Optional;

import org.example.quiz.model.Session;
import org.example.quiz.model.User;
import org.example.quiz.service.SessionManager;
import org.example.quiz.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SERVICE API
 * Controller REST pour l'authentification (login, verify, logout).
 * Sans dépendance externe - sécurité minimale pour projet universitaire.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRegistry userRegistry;
    private final SessionManager sessionManager;

    // Injection des dépendances via constructeur
    @Autowired
    public AuthController(UserRegistry userRegistry, SessionManager sessionManager) {
        this.userRegistry = userRegistry;
        this.sessionManager = sessionManager;
    }

    // ==================== ENDPOINTS ====================

    /**
     * LOGIN - Authentifie un utilisateur et crée une session.
     * POST /api/auth/login?username=xxx&password=xxx
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = userRegistry.authenticate(username, password);
        
        if (user.isEmpty()) {
            return new LoginResponse(false, null, null, "Invalid credentials");
        }
        
        String token = sessionManager.createSession(user.get());
        return new LoginResponse(true, token, user.get().authority(), null);
    }

    /**
     * VERIFY - Vérifie si un token est valide.
     * GET /api/auth/verify?token=xxx
     */
    @GetMapping("/verify")
    public VerifyResponse verify(@RequestParam String token) {
        Optional<Session> session = sessionManager.verify(token);
        
        if (session.isEmpty()) {
            return new VerifyResponse(false, null, null, "Invalid or expired token");
        }
        
        // Récupère l'autorité de l'utilisateur
        String authority = userRegistry.findByUsername(session.get().username())
                .map(User::authority)
                .orElse("UNKNOWN");
        
        return new VerifyResponse(true, session.get().username(), authority, null);
    }

    /**
     * LOGOUT - Invalide un token.
     * POST /api/auth/logout?token=xxx
     */
    @PostMapping("/logout")
    public LogoutResponse logout(@RequestParam String token) {
        sessionManager.invalidate(token);
        return new LogoutResponse(true, "Session terminated");
    }

    /**
     * REGISTER - Enregistre un nouvel utilisateur (optionnel).
     * POST /api/auth/register?username=xxx&password=xxx
     */
    @PostMapping("/register")
    public RegisterResponse register(@RequestParam String username, @RequestParam String password) {
        if (userRegistry.exists(username)) {
            return new RegisterResponse(false, "Username already exists");
        }
        
        userRegistry.register(username, password, "USER");
        return new RegisterResponse(true, "User registered successfully");
    }

    // ==================== DTOs (Records) ====================

    public record LoginResponse(boolean success, String token, String authority, String error) {}
    public record VerifyResponse(boolean valid, String username, String authority, String error) {}
    public record LogoutResponse(boolean success, String message) {}
    public record RegisterResponse(boolean success, String message) {}
}
