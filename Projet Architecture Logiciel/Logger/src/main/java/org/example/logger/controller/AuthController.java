package org.example.logger.controller;

import java.util.List;
import java.util.Optional;

import org.example.logger.model.Authority;
import org.example.logger.model.Credentials;
import org.example.logger.model.Session;
import org.example.logger.model.User;
import org.example.logger.service.SessionManager;
import org.example.logger.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<LoginResponse> login(@RequestParam String username, @RequestParam String password) {
        Credentials inputCredentials = new Credentials(password);
        Optional<User> userOpt = userRegistry.findByUsername(username);

        if (userOpt.isEmpty() || !userOpt.get().credentials().matches(inputCredentials)) {
            return ResponseEntity.status(401).body(
                new LoginResponse(false, null, null, "Invalid credentials")
            );
        }

        User user = userOpt.get();
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body(
                new LoginResponse(false, null, null, "Account disabled or not verified")
            );
        }

        if (!user.authority().hasRole(Authority.Role.ROLE_LOGGER) && !user.authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return ResponseEntity.status(403).body(
                new LoginResponse(false, null, null, "Forbidden: You don't have the ROLE_LOGGER to authenticate")
            );
        }

        String token = sessionManager.createSession(user);
        return ResponseEntity.ok(new LoginResponse(true, token, user.authority().getName(), null));
    }

    @GetMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestParam String token) {
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return ResponseEntity.status(400).body(
                new VerifyResponse(false, null, null, "Invalid or expired token")
            );
        }

        String username = session.get().username();
        userRegistry.enableUser(username);
        String authority = userRegistry.findByUsername(username)
                .map(u -> u.authority().getName()) 
                .orElse("UNKNOWN");

        return ResponseEntity.ok(new VerifyResponse(true, username, authority, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new LogoutResponse(false, "Missing or invalid Authorization header"));
        }
        
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        
        if (session.isPresent()) {
            Optional<User> user = userRegistry.findByUsername(session.get().username());
            if (user.isPresent() && user.get().authority().hasRole(Authority.Role.ROLE_ADMIN)) {
                return ResponseEntity.status(403).body(new LogoutResponse(false, "Admin sessions cannot be terminated via logout"));
            }
        }
        
        sessionManager.invalidate(token);
        return ResponseEntity.ok(new LogoutResponse(true, "Session terminated"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "requiredRole", required = false) String requiredRole) {
            
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("{\"error\": \"Missing or invalid Authorization header\"}");
        }
        
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return ResponseEntity.status(401).body("{\"error\": \"Invalid or expired session\"}");
        }
        
        String username = session.get().username();
        Optional<User> user = userRegistry.findByUsername(username);
        
        if (user.isEmpty() || !user.get().isEnabled()) {
            return ResponseEntity.status(403).body("{\"error\": \"User not enabled or not found\"}");
        }
        
        if (requiredRole != null && !requiredRole.isBlank()) {
            if (!user.get().authority().hasRole(requiredRole)) {
                return ResponseEntity.status(403).body("{\"error\": \"Forbidden: Insufficient privileges for this service\"}");
            }
        }
        
        return ResponseEntity.ok("Authorized");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestParam String username, @RequestParam String password) {
        if (userRegistry.exists(username)) {
            return ResponseEntity.status(409).body(new RegisterResponse(false, "Username already exists"));
        }
        
        Credentials creds = new Credentials(password);
        Authority auth = new Authority(Authority.Role.ROLE_LOGGER);
        userRegistry.register(username, creds, auth);
        
        return ResponseEntity.status(201).body(new RegisterResponse(true, "User registered successfully"));
    }

    @PostMapping("/manage-roles")
    public ResponseEntity<String> manageRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String targetUsername,
            @RequestParam String action,
            @RequestParam String role) {
            
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }
        
        Optional<User> adminUser = userRegistry.findByUsername(session.get().username());
        if (adminUser.isEmpty() || !adminUser.get().authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return ResponseEntity.status(403).body("Forbidden: Admin rights required");
        }
        
        Authority.Role roleEnum;
        try {
            roleEnum = Authority.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        
        Optional<User> targetUserOpt = userRegistry.findByUsername(targetUsername);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Target user not found");
        }
        
        User targetUser = targetUserOpt.get();
        Authority targetAuthority = targetUser.authority();
        
        if ("add".equalsIgnoreCase(action)) {
            targetAuthority.addRole(roleEnum);
        } else if ("remove".equalsIgnoreCase(action)) {
            targetAuthority.removeRole(roleEnum);
        } else {
            return ResponseEntity.badRequest().body("Invalid action");
        }
        
        targetUser.setAuthority(targetAuthority);
        userRegistry.updateUser(targetUser);
        
        return ResponseEntity.ok("Role " + roleEnum.name() + " successfully " + action + "ed for user " + targetUsername);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }
        
        Optional<User> userOpt = userRegistry.findByUsername(session.get().username());
        if (userOpt.isEmpty() || !userOpt.get().isEnabled()) {
            return ResponseEntity.status(403).body("Account not verified or disabled");
        }
        
        User user = userOpt.get();
        if (!user.authority().hasRole(Authority.Role.ROLE_LOGGER) && !user.authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return ResponseEntity.status(403).body("Forbidden: ROLE_LOGGER or ROLE_ADMIN required to view user list");
        }
        
        List<UserDTO> userDTOs = userRegistry.findAllUsers().stream()
                .map(u -> new UserDTO(u.username(), u.isEnabled(), u.authority().getName()))
                .toList();
                
        return ResponseEntity.ok(userDTOs);
    }

    public record LoginResponse(boolean success, String token, String authority, String error) {}
    public record VerifyResponse(boolean valid, String username, String authority, String error) {}
    public record LogoutResponse(boolean success, String message) {}
    public record RegisterResponse(boolean success, String message) {}
    public record UserDTO(String username, boolean enabled, String authority) {}
}