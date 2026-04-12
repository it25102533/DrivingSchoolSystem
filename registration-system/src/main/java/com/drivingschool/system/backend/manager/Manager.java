package com.drivingschool.system.backend.manager;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "managers")
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String username;

    /** BCrypt hash — never store plain text. */
    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    /** Login identifier — unique, used for professional sign-in. */
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    /** Optional label shown on dashboard (e.g. Operations, Fleet). */
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ManagerRole role;

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public ManagerRole getRole() {
        return role;
    }

    public void setRole(ManagerRole role) {
        this.role = role;
    }
}
