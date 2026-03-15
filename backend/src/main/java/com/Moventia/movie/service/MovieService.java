package com.Moventia.movie.service;

import com.Moventia.movie.client.TmdbClient;
import com.Moventia.movie.dto.MovieResponse;
import com.Moventia.movie.dto.TmdbMovieResponse;
import com.Moventia.movie.dto.TmdbPageResponse;
import com.Moventia.movie.model.Movie;
import com.Moventia.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbClient tmdbClient;
    private final String imageBaseUrl;

    public MovieService(
            MovieRepository movieRepository,
            TmdbClient tmdbClient,
            @Value("${tmdb.image.base-url}") String imageBaseUrl
    ) {
        this.movieRepository = movieRepository;
        this.tmdbClient = tmdbClient;
        this.imageBaseUrl = imageBaseUrl;
    }

    // ── Cache-aside: the core method called before any interaction ───────────
    // Every other feature (reviews, favorites) calls this first
    public Movie getOrFetchMovie(Integer tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    // Cache miss — fetch from TMDB and save
                    TmdbMovieResponse tmdbData = tmdbClient.fetchMovieById(tmdbId);
                    return movieRepository.save(mapToEntity(tmdbData));
                });
    }

    // ── GET /api/movies/{tmdbId} — movie detail page ─────────────────────────
    public MovieResponse getMovieDetail(Integer tmdbId) {
        // Always fetch full details from TMDB for detail page (genres, runtime etc)
        // but ensure it's saved in our DB
        TmdbMovieResponse tmdbData = tmdbClient.fetchMovieById(tmdbId);
        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> movieRepository.save(mapToEntity(tmdbData)));

        return buildResponse(movie, tmdbData);
    }

    // ── GET /api/movies/trending ─────────────────────────────────────────────
    public List<MovieResponse> getTrending(int page) {
        TmdbPageResponse tmdbPage = tmdbClient.getTrending(page);
        return tmdbPage.getResults().stream()
                .map(tmdbMovie -> buildResponse(null, tmdbMovie))
                .collect(Collectors.toList());
    }

    // ── GET /api/movies/search?q= ────────────────────────────────────────────
    public List<MovieResponse> searchMovies(String query, int page) {
        TmdbPageResponse tmdbPage = tmdbClient.searchMovies(query, page);
        return tmdbPage.getResults().stream()
                .map(tmdbMovie -> buildResponse(null, tmdbMovie))
                .collect(Collectors.toList());
    }

    // ── GET /api/movies/discover ─────────────────────────────────────────────
    // language: "en" = Hollywood, "hi" = Bollywood, "ta" = Tamil, null = all
    public List<MovieResponse> discoverMovies(String language, String sortBy, int page) {
        TmdbPageResponse tmdbPage = tmdbClient.discoverMovies(language, sortBy, page);
        return tmdbPage.getResults().stream()
                .map(tmdbMovie -> buildResponse(null, tmdbMovie))
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // Save only the fields we need for FK relationships and list display
    private Movie mapToEntity(TmdbMovieResponse tmdb) {
        return Movie.builder()
                .tmdbId(tmdb.getId())
                .title(tmdb.getTitle())
                .overview(tmdb.getOverview())
                .posterPath(tmdb.getPosterPath())
                .backdropPath(tmdb.getBackdropPath())
                .releaseDate(tmdb.getReleaseDate())
                .originalLanguage(tmdb.getOriginalLanguage())
                .tmdbRating(tmdb.getVoteAverage())
                .build();
    }

    // Build response — movie can be null for list views (not yet in our DB)
    // tmdbData is the live TMDB response
    public MovieResponse buildResponse(Movie movie, TmdbMovieResponse tmdb) {
        // Use our DB stats if available, otherwise zeros
        int reviewCount = movie != null ? movie.getReviewCount() : 0;
        Double avgRating = movie != null ? movie.getAverageRating() : 0.0;
        Long dbId = movie != null ? movie.getId() : null;

        // Format runtime e.g. 148 → "2h 28m"
        String runtime = null;
        if (tmdb.getRuntime() != null && tmdb.getRuntime() > 0) {
            int hours = tmdb.getRuntime() / 60;
            int mins = tmdb.getRuntime() % 60;
            runtime = (hours > 0 ? hours + "h " : "") + mins + "m";
        }

        // Extract genre names from list of genre objects
        List<String> genres = null;
        if (tmdb.getGenres() != null) {
            genres = tmdb.getGenres().stream()
                    .map(TmdbMovieResponse.Genre::getName)
                    .collect(Collectors.toList());
        }

        return MovieResponse.builder()
                .id(dbId)
                .tmdbId(tmdb.getId())
                .title(tmdb.getTitle())
                .overview(tmdb.getOverview())
                .posterUrl(tmdb.getPosterPath() != null ? imageBaseUrl + tmdb.getPosterPath() : null)
                .backdropUrl(tmdb.getBackdropPath() != null ? imageBaseUrl + tmdb.getBackdropPath() : null)
                .releaseDate(tmdb.getReleaseDate())
                .originalLanguage(tmdb.getOriginalLanguage())
                .tmdbRating(tmdb.getVoteAverage())
                .runtime(runtime)
                .genres(genres)
                .reviewCount(reviewCount)
                .averageRating(avgRating)
                .build();
    }
}
