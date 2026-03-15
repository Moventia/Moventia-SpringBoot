package com.Moventia.movie.repository;

import com.Moventia.movie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Cache-aside lookup — the core method used everywhere
    Optional<Movie> findByTmdbId(Integer tmdbId);

    boolean existsByTmdbId(Integer tmdbId);

    // For browse page — returns movies already in our DB
    List<Movie> findAllByOrderByAverageRatingDesc();
}
