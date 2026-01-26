package org.example.quiz.model;

/**
 * Représente un utilisateur avec son identité et son autorité (rôle).
 * Record Java 17+ : immuable, génère automatiquement equals, hashCode, toString.
 */
public record User(
    String username,
    String password,  // En production, ce serait un hash !
    String authority  // Ex: "USER", "ADMIN"
) {}
