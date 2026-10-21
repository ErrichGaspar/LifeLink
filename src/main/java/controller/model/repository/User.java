package controller.model.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    // =========================
    // ID
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // USER INFORMATION
    // =========================

    private String firstName;

    private String lastName;


    @Column(unique = true)
    private String username;


    private String password;

    private String phoneNumber;

    private String bloodType;

    private String role;

    private String rememberToken;

    /** Stable public-facing donor identifier derived from the immutable database key. */
    @jakarta.persistence.Transient
    public String getDonorId() { return id == null ? "Pending" : "LL-%06d".formatted(id); }


    // =========================
    // GETTERS AND SETTERS
    // =========================

    public Long getId() {

        return id;

    }


    public void setId(Long id) {

        this.id = id;

    }


    // =========================
    // FIRST NAME
    // =========================

    public String getFirstName() {

        return firstName;

    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;

    }


    // =========================
    // LAST NAME
    // =========================

    public String getLastName() {

        return lastName;

    }


    public void setLastName(String lastName) {

        this.lastName = lastName;

    }


    // =========================
    // USERNAME
    // =========================

    public String getUsername() {

        return username;

    }


    public void setUsername(String username) {

        this.username = username;

    }


    // =========================
    // PASSWORD
    // =========================

    public String getPassword() {

        return password;

    }


    public void setPassword(String password) {

        this.password = password;

    }


    // =========================
    // PHONE NUMBER
    // =========================

    public String getPhoneNumber() {

        return phoneNumber;

    }


    public void setPhoneNumber(String phoneNumber) {

        this.phoneNumber = phoneNumber;

    }


    // =========================
    // BLOOD TYPE
    // =========================

    public String getBloodType() {

        return bloodType;

    }


    public void setBloodType(String bloodType) {

        this.bloodType = bloodType;

    }
  
    // =========================
    // ROLE
    // =========================

    public String getRole() {

        return role;

    }


    public void setRole(String role) {

        this.role = role;

    }

    public String getRememberToken() { return rememberToken; }
    public void setRememberToken(String rememberToken) { this.rememberToken = rememberToken; }

}
