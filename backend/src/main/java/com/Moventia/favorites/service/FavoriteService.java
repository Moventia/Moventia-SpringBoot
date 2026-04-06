package com.Moventia.favorites.service;

import com.Moventia.favorites.dto.FavoriteResponse;
import com.Moventia.favorites.entity.UserFavorite;
import com.Moventia.favorites.repository.UserFavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;

    public FavoriteService(UserFavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // ── Add to favorites ──────────────────────────────────────────────────────
    @Transactional
    public FavoriteResponse addFavorite(String username, Long tmdbId) {
        if (favoriteRepository.existsByUsernameAndTmdbId(username, tmdbId)) {
            throw new IllegalStateException("Movie is already in favorites");
        }

        UserFavorite favorite = UserFavorite.builder()
                .username(username)
                .tmdbId(tmdbId)
                .build();

        UserFavorite saved = favoriteRepository.save(favorite);
        return toResponse(saved);
    }

    // ── Remove from favorites ─────────────────────────────────────────────────
    @Transactional
    public void removeFavorite(String username, Long tmdbId) {
        if (!favoriteRepository.existsByUsernameAndTmdbId(username, tmdbId)) {
            throw new IllegalStateException("Movie is not in favorites");
        }
        favoriteRepository.deleteByUsernameAndTmdbId(username, tmdbId);
    }

    // ── Get own favorites (JWT protected) ────────────────────────────────────
    public List<FavoriteResponse> getMyFavorites(String username) {
        return favoriteRepository
                .findByUsernameOrderBySavedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Get user's favorites (public) ─────────────────────────────────────────
    public List<FavoriteResponse> getUserFavorites(String username) {
        return favoriteRepository
                .findByUsernameOrderBySavedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Check if a movie is favorited (for UI toggle state) ───────────────────
    public boolean isFavorited(String username, Long tmdbId) {
        return favoriteRepository.existsByUsernameAndTmdbId(username, tmdbId);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────
    private FavoriteResponse toResponse(UserFavorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .username(favorite.getUsername())
                .tmdbId(favorite.getTmdbId())
                .savedAt(favorite.getSavedAt())
                .build();
    }
}
