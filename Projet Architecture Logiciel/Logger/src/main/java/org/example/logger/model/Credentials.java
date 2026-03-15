package org.example.logger.model;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
@Embeddable
public class Credentials {
    @Column(name = "password_value") 
    private String value; 
    protected Credentials() {
    }
    public Credentials(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Credentials value cannot be null");
        }
        this.value = value;
    }
    public boolean matches(Credentials other) {
        if (other == null) return false;
        return this.value.equals(other.value);
    }
    @Override
    public String toString() {
        return "******"; 
    }
}
