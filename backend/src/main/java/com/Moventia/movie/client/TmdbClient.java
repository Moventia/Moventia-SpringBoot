package com.Moventia.movie.client;

import com.Moventia.movie.dto.TmdbMovieResponse;
import com.Moventia.movie.dto.TmdbPageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Thin wrapper around TMDB REST API.
 * All TMDB calls go through here — nowhere else in the codebase
 * should construct TMDB URLs directly.
 */
@Component
public class TmdbClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public TmdbClient(
            RestTemplate restTemplate,
            @Value("${tmdb.api.key}") String apiKey,
            @Value("${tmdb.api.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    // ── Fetch single movie by TMDB id ────────────────────────────────────────
    public TmdbMovieResponse fetchMovieById(Integer tmdbId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .toUriString();

        return restTemplate.getForObject(url, TmdbMovieResponse.class);
    }

    // ── Search movies by title ───────────────────────────────────────────────
    public TmdbPageResponse searchMovies(String query, int page) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("page", page)
                .toUriString();

        return restTemplate.getForObject(url, TmdbPageResponse.class);
    }

    // ── Trending movies (for home page) ─────────────────────────────────────
    public TmdbPageResponse getTrending(int page) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/trending/movie/week")
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .toUriString();

        return restTemplate.getForObject(url, TmdbPageResponse.class);
    }

    // ── Discover with filters (genre, language, etc) ─────────────────────────
    public TmdbPageResponse discoverMovies(String language, String sortBy, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl + "/discover/movie")
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .queryParam("sort_by", sortBy != null ? sortBy : "popularity.desc");

        // if language filter provided e.g. "hi" for Hindi, "en" for English
        if (language != null && !language.isBlank()) {
            builder.queryParam("with_original_language", language);
        }

        return restTemplate.getForObject(builder.toUriString(), TmdbPageResponse.class);
    }
}
