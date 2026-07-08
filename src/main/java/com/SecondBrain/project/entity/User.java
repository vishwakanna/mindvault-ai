package com.SecondBrain.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity                          // Marks this class as a DB table
@Table(name = "users")           // Table name in PostgreSQL
@Data                            // Lombok: generates getters, setters, toString, equals, hashCode
@Builder                         // Lombok: enables User.builder().email("x").build() pattern
@NoArgsConstructor               // Lombok: generates default constructor (JPA requires this)
@AllArgsConstructor              // Lombok: generates constructor with all fields
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment like SERIAL in SQL
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)  // unique = no two users share an email
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;    // Will store bcrypt hash, NEVER plaintext

    @Enumerated(EnumType.STRING)  // Store enum as string "USER" in DB, not 0/1
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist  // Called automatically BEFORE INSERT
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }
}