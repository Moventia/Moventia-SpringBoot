package com.Moventia.review.model;

import com.Moventia.movie.model.Movie;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "movie_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to users.username
    @Column(nullable = false)
    private String username;

    // FK to movies table — cache-aside ensures this always exists
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private int rating;             // 1-5 stars

    @Column(nullable = false)
    private String title;           // one line summary

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // full review text

    @Builder.Default
    private boolean hasSpoilers = false;

    @Builder.Default
    private int likeCount = 0;      // denormalized count for fast reads

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
