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
        Credentials inputCredentials = new Credentials(password);
        Optional<User> userOpt = userRegistry.authenticate(username, inputCredentials);
        if (userOpt.isEmpty()) {
            return new LoginResponse(false, null, null, "Invalid credentials or account disabled");
        }
        User user = userOpt.get();
        if (!user.authority().hasRole(Authority.Role.ROLE_LOGGER) && !user.authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return new LoginResponse(false, null, null, "Forbidden: You don't have the ROLE_LOGGER to authenticate");
        }
        String token = sessionManager.createSession(user);
        return new LoginResponse(true, token, user.authority().getName(), null);
    }
    @GetMapping("/verify")
    public VerifyResponse verify(@RequestParam String token) {
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return new VerifyResponse(false, null, null, "Invalid or expired token");
        }
        String username = session.get().username();
        userRegistry.enableUser(username);
        String authority = userRegistry.findByUsername(username)
                .map(u -> u.authority().getName()) 
                .orElse("UNKNOWN");
        return new VerifyResponse(true, username, authority, null);
    }
    @PostMapping("/logout")
    public org.springframework.http.ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isPresent()) {
            Optional<User> user = userRegistry.findByUsername(session.get().username());
            if (user.isPresent() && user.get().authority().hasRole(Authority.Role.ROLE_ADMIN)) {
                return org.springframework.http.ResponseEntity.status(403).body("Admin sessions cannot be terminated via logout");
            }
        }
        sessionManager.invalidate(token);
        return org.springframework.http.ResponseEntity.ok(new LogoutResponse(true, "Session terminated"));
    }
    @GetMapping("/validate")
    public org.springframework.http.ResponseEntity<?> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "requiredRole", required = false) String requiredRole) {
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
        if (requiredRole != null && !requiredRole.isBlank()) {
            if (!user.get().authority().hasRole(requiredRole)) {
                return org.springframework.http.ResponseEntity.status(403).body("Forbidden: Insufficient privileges for this service");
            }
        }
        return org.springframework.http.ResponseEntity.ok("Authorized");
    }
    @PostMapping("/register")
    public RegisterResponse register(@RequestParam String username, @RequestParam String password) {
        if (userRegistry.exists(username)) {
            return new RegisterResponse(false, "Username already exists");
        }
        Credentials creds = new Credentials(password);
        Authority auth = new Authority(Authority.Role.ROLE_LOGGER);
        userRegistry.register(username, creds, auth);
        return new RegisterResponse(true, "User registered successfully");
    }
    @PostMapping("/manage-roles")
    public org.springframework.http.ResponseEntity<?> manageRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String targetUsername,
            @RequestParam String action,
            @RequestParam String role) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(401).body("Invalid or expired session");
        }
        Optional<User> adminUser = userRegistry.findByUsername(session.get().username());
        if (adminUser.isEmpty() || !adminUser.get().authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden: Admin rights required");
        }
        Authority.Role roleEnum;
        try {
            roleEnum = Authority.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body("Invalid role. Possible values: ROLE_SERVICE_A, ROLE_SERVICE_B, ROLE_LOGGER, ROLE_MESSAGERIE, ROLE_ADMIN");
        }
        Optional<User> targetUserOpt = userRegistry.findByUsername(targetUsername);
        if (targetUserOpt.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(404).body("Target user not found");
        }
        User targetUser = targetUserOpt.get();
        Authority targetAuthority = targetUser.authority();
        if ("add".equalsIgnoreCase(action)) {
            targetAuthority.addRole(roleEnum);
        } else if ("remove".equalsIgnoreCase(action)) {
            targetAuthority.removeRole(roleEnum);
        } else {
            return org.springframework.http.ResponseEntity.badRequest().body("Invalid action. Use 'add' or 'remove'.");
        }
        targetUser.setAuthority(targetAuthority);
        userRegistry.updateUser(targetUser);
        return org.springframework.http.ResponseEntity.ok("Role " + roleEnum.name() + " successfully " + action + "ed for user " + targetUsername);
    }
    @GetMapping("/users")
    public org.springframework.http.ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Optional<Session> session = sessionManager.verify(token);
        if (session.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(401).body("Invalid or expired session");
        }
        Optional<User> userOpt = userRegistry.findByUsername(session.get().username());
        if (userOpt.isEmpty() || !userOpt.get().isEnabled()) {
            return org.springframework.http.ResponseEntity.status(403).body("Account not verified or disabled");
        }
        User user = userOpt.get();
        if (!user.authority().hasRole(Authority.Role.ROLE_LOGGER) && !user.authority().hasRole(Authority.Role.ROLE_ADMIN)) {
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden: ROLE_LOGGER or ROLE_ADMIN required to view user list");
        }
        java.util.List<UserDTO> userDTOs = userRegistry.findAllUsers().stream()
                .map(u -> new UserDTO(u.username(), u.isEnabled(), u.authority().getName()))
                .toList();
        return org.springframework.http.ResponseEntity.ok(userDTOs);
    }
    public record LoginResponse(boolean success, String token, String authority, String error) {}
    public record VerifyResponse(boolean valid, String username, String authority, String error) {}
    public record LogoutResponse(boolean success, String message) {}
    public record RegisterResponse(boolean success, String message) {}
    public record UserDTO(String username, boolean enabled, String authority) {}
}