package com.Moventia.favorites.repository;

import com.Moventia.favorites.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    List<UserFavorite> findByUsernameOrderBySavedAtDesc(String username);

    Optional<UserFavorite> findByUsernameAndTmdbId(String username, Long tmdbId);

    boolean existsByUsernameAndTmdbId(String username, Long tmdbId);

    void deleteByUsernameAndTmdbId(String username, Long tmdbId);
}
