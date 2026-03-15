package com.Moventia.review.repository;

import com.Moventia.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews for a specific movie
    List<Review> findByMovieTmdbIdOrderByCreatedAtDesc(Integer tmdbId);

    // All reviews by a specific user
    List<Review> findByUsernameOrderByCreatedAtDesc(String username);

    // Check if user already reviewed this movie (for unique constraint enforcement)
    boolean existsByUsernameAndMovieTmdbId(String username, Integer tmdbId);

    // Find specific review to allow delete + re-review
    Optional<Review> findByUsernameAndMovieTmdbId(String username, Integer tmdbId);

    // Feed query — reviews from a specific list of usernames, newest first
    Page<Review> findByUsernameInOrderByCreatedAtDesc(List<String> usernames, Pageable pageable);

    // Count reviews for a movie — used to update Movie.reviewCount
    int countByMovieTmdbId(Integer tmdbId);
}
