package com.Moventia.movie.controller;

import com.Moventia.movie.dto.MovieResponse;
import com.Moventia.movie.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ── GET /api/movies/{tmdbId} ─────────────────────────────────────────────
    // Movie detail page — full info including genres, runtime
    // Also triggers cache-aside save if not in DB yet
    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable Integer tmdbId) {
        return ResponseEntity.ok(movieService.getMovieDetail(tmdbId));
    }

    // ── GET /api/movies/trending?page=1 ─────────────────────────────────────
    // Home page hero + trending section
    @GetMapping("/trending")
    public ResponseEntity<List<MovieResponse>> getTrending(
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getTrending(page));
    }

    // ── GET /api/movies/search?q=inception&page=1 ────────────────────────────
    // Search bar
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.searchMovies(q, page));
    }

    // ── GET /api/movies/discover?language=hi&sortBy=popularity.desc&page=1 ───
    // Browse page with filters
    // language: "en" = English/Hollywood, "hi" = Hindi/Bollywood,
    //           "ta" = Tamil, "te" = Telugu, leave empty for all
    // sortBy: "popularity.desc", "vote_average.desc", "release_date.desc"
    @GetMapping("/discover")
    public ResponseEntity<List<MovieResponse>> discover(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.discoverMovies(language, sortBy, page));
    }
}
