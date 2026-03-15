package org.example.logger.model;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "app_user")
public class User {
    @Id
    private String username;
    @Embedded
    private Credentials credentials;
    @Embedded
    private Authority authority;
    private boolean enabled;
    public User() {
    }
    public User(String username, Credentials credentials, Authority authority) {
        this.username = username;
        this.credentials = credentials;
        this.authority = authority;
        this.enabled = false; 
    }
    public String username() {
        return username;
    }
    public Credentials credentials() {
        return credentials;
    }
    public Authority authority() {
        return authority;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setAuthority(Authority authority) {
        this.authority = authority;
    }
}
