package com.Moventia.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank
    @Size(min = 3, max = 30)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    // Stored as BCrypt hash — NEVER store plain text
    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String fullName, String username, String email, String password) {
        this.fullName  = fullName;
        this.username  = username;
        this.email     = email;
        this.password  = password;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                    { return id; }

    public String getFullName()            { return fullName; }
    public void   setFullName(String v)    { this.fullName = v; }

    public String getUsername()            { return username; }
    public void   setUsername(String v)    { this.username = v; }

    public String getEmail()               { return email; }
    public void   setEmail(String v)       { this.email = v; }

    public String getPassword()            { return password; }
    public void   setPassword(String v)    { this.password = v; }

    public LocalDateTime getCreatedAt()    { return createdAt; }

    public boolean isActive()              { return isActive; }
    public void    setActive(boolean v)    { this.isActive = v; }
}
