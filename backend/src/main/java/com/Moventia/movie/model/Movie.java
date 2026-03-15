package com.Moventia.movie.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer tmdbId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private String posterPath;       // e.g. "/abc123.jpg" — prefix with tmdb image base url
    private String backdropPath;
    private String releaseDate;
    private String originalLanguage; // "en", "hi", "ta" etc

    private Double tmdbRating;       // TMDB's own vote average

    // ── Your app's own stats (updated when reviews are posted) ──
    @Builder.Default
    private int reviewCount = 0;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
}
