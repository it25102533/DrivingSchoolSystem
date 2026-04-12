package com.drivingschool.system.backend.it25102533;

import com.drivingschool.system.backend.shared.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int age;

    private String licenseType;

    /** "Corporate" gets a larger discount in {@link #calculateDiscount(double)}. */
    private String studentType;

    /** Unique login email (nullable for legacy/admin-created rows). */
    @Column(unique = true, length = 128)
    private String email;

    /** BCrypt hash; null until student signs up or password is set. */
    @Column(length = 120)
    private String passwordHash;

    @Column(length = 500)
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** Returns the discount amount (not the final price) for polymorphic pricing. */
    public double calculateDiscount(double basePrice) {
        if (studentType != null && "Corporate".equalsIgnoreCase(studentType)) {
            return basePrice * 0.2;
        }
        return basePrice * 0.1;
    }
}
