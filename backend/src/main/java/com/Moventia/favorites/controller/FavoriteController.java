package com.Moventia.favorites.controller;

import com.Moventia.favorites.dto.FavoriteResponse;
import com.Moventia.favorites.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // ── POST /api/favorites/{tmdbId} ── JWT required ──────────────────────────
    @PostMapping("/{tmdbId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long tmdbId,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            FavoriteResponse response = favoriteService.addFavorite(username, tmdbId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ── DELETE /api/favorites/{tmdbId} ── JWT required ────────────────────────
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long tmdbId,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            favoriteService.removeFavorite(username, tmdbId);
            return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ── GET /api/favorites ── JWT required ────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites(
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(favoriteService.getMyFavorites(username));
    }

    // ── GET /api/favorites/{username} ── Public ───────────────────────────────
    @GetMapping("/{username}")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(username));
    }

    // ── GET /api/favorites/status/{tmdbId} ── JWT required ───────────────────
    // Lets the frontend know whether to show a filled or empty heart icon
    @GetMapping("/status/{tmdbId}")
    public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(
            @PathVariable Long tmdbId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean favorited = favoriteService.isFavorited(username, tmdbId);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }
}
