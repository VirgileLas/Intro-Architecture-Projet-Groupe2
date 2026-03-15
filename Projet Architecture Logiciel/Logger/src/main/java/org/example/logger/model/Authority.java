package org.example.logger.model;
import java.util.Objects;
import jakarta.persistence.Embeddable;
@Embeddable
public class Authority {
    public enum Role {
        ROLE_SERVICE_A,
        ROLE_SERVICE_B,
        ROLE_LOGGER,
        ROLE_MESSAGERIE,
        ROLE_ADMIN
    }
    private String name; 
    protected Authority() {
    }
    public Authority(Role... roles) {
        StringBuilder sb = new StringBuilder();
        for (Role r : roles) {
            if (sb.length() > 0) sb.append(",");
            sb.append(r.name());
        }
        this.name = sb.toString();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Authority name cannot be empty");
        }
    }
    public Authority(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Authority name cannot be empty");
        }
        this.name = name.toUpperCase(); 
    }
    public String getName() {
        return name;
    }
    public boolean hasRole(String role) {
        if (role == null || role.isBlank()) return false;
        if (this.name == null) return false;
        String[] roles = this.name.split(",");
        for (String r : roles) {
            if (r.trim().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasRole(Role role) {
        return role != null && hasRole(role.name());
    }
    public void addRole(String role) {
        if (!hasRole(role)) {
            if (this.name == null || this.name.isBlank()) {
                this.name = role;
            } else {
                this.name = this.name + "," + role;
            }
        }
    }
    public void addRole(Role role) {
        if (role != null) addRole(role.name());
    }
    public void removeRole(String role) {
        if (hasRole(role)) {
            String[] roles = this.name.split(",");
            StringBuilder newRoles = new StringBuilder();
            for (String r : roles) {
                if (!r.trim().equalsIgnoreCase(role)) {
                    if (newRoles.length() > 0) newRoles.append(",");
                    newRoles.append(r.trim());
                }
            }
            this.name = newRoles.toString();
        }
    }
    public void removeRole(Role role) {
        if (role != null) removeRole(role.name());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return Objects.equals(name, authority.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}