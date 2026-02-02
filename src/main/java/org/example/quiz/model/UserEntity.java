package org.example.quiz.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ENTITÉ JPA - Représente un utilisateur persisté en base de données.
 * 
 * Annotations JPA :
 * - @Entity : marque cette classe comme entité persistante
 * - @Table : définit le nom de la table en BDD
 * - @Id : clé primaire
 * - @GeneratedValue : auto-incrémentation de l'ID
 * - @Column : configuration des colonnes
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;  // En production : BCrypt hash !

    @Column(nullable = false, length = 20)
    private String authority;  // Ex: "USER", "ADMIN"

    // ==================== Constructeurs ====================

    /**
     * Constructeur par défaut requis par JPA.
     */
    public UserEntity() {
    }

    /**
     * Constructeur avec tous les champs (sans ID, généré automatiquement).
     */
    public UserEntity(String username, String password, String authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    // ==================== Méthodes utilitaires ====================

    /**
     * Convertit l'entité en record User (DTO immuable).
     */
    public User toUser() {
        return new User(username, password, authority);
    }

    /**
     * Crée une entité à partir d'un record User.
     */
    public static UserEntity fromUser(User user) {
        return new UserEntity(user.username(), user.password(), user.authority());
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", authority='" + authority + '\'' +
                '}';
    }
}
