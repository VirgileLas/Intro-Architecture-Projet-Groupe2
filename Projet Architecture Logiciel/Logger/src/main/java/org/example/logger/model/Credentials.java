package org.example.logger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Encapsule les informations de sécurité (mot de passe).
 * Permet d'abstraire la logique de stockage (String, Hash, etc.)
 * et la logique de vérification.
 */
@Embeddable
public class Credentials {
    
    @Column(name = "password_value") // 'value' est réservé dans H2
    private String value; 

    protected Credentials() {
        // JPA requires a no-args constructor
    }

    public Credentials(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Credentials value cannot be null");
        }
        this.value = value;
    }

    /**
     * Vérifie si les identifiants fournis correspondent à ceux stockés.
     * C'est ici qu'on mettra plus tard la logique de hashage (ex: BCrypt).
     */
    public boolean matches(Credentials other) {
        if (other == null) return false;
        return this.value.equals(other.value);
    }
    
    // On évite d'exposer le getter du mot de passe brut si possible, 
    // mais pour l'instant utile pour le debug ou stockage simple.
    @Override
    public String toString() {
        return "******"; // Sécurité : on n'affiche jamais le mdp dans les logs
    }
}
