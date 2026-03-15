package com.Moventia.review.controller;

import com.Moventia.review.dto.ReviewRequest;
import com.Moventia.review.dto.ReviewResponse;
import com.Moventia.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ── POST /api/reviews ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> submitReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        try {
            ReviewResponse response = reviewService.submitReview(
                    authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ── GET /api/reviews/movie/{tmdbId} ──────────────────────────────────────
    @GetMapping("/movie/{tmdbId}")
    public ResponseEntity<List<ReviewResponse>> getMovieReviews(
            @PathVariable Integer tmdbId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(reviewService.getMovieReviews(tmdbId, email));
    }

    // ── GET /api/reviews/user/{username} ─────────────────────────────────────
    @GetMapping("/user/{username}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @PathVariable String username,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(reviewService.getUserReviews(username, email));
    }

    // ── DELETE /api/reviews/{id} ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reviewService.deleteReview(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/reviews/{id}/like ──────────────────────────────────────────
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeReview(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reviewService.likeReview(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    // ── DELETE /api/reviews/{id}/like ────────────────────────────────────────
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeReview(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reviewService.unlikeReview(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    // ── GET /api/feed?page=0&size=10 ─────────────────────────────────────────
    @GetMapping("/feed")
    public ResponseEntity<List<ReviewResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                reviewService.getFeed(authentication.getName(), page, size));
    }
}
