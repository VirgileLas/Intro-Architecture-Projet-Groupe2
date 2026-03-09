package org.example.logger.model;

import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * Encapsule les droits/rôles d'un utilisateur.
 */
@Embeddable
public class Authority {
    
    private String name; // Changed to non-final for JPA

    protected Authority() {
        // JPA requires a no-args constructor
    }

    public Authority(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Authority name cannot be empty");
        }
        this.name = name.toUpperCase(); // Normalisation
    }

    public String getName() {
        return name;
    }

    // Utile pour comparer des droits plus tard
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