package com.Moventia.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    // ── NEW profile fields ──────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private int followerCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int followingCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int reviewCount = 0;
    // ────────────────────────────────────────────────────────

//    @Builder.Default
//    @Column(name = "is_active", nullable = false)
//    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
