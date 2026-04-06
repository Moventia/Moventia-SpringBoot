package com.Moventia.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the frontend receives — never expose the raw TMDB or entity directly.
 * posterUrl is the FULL url (base + path), frontend doesn't need to construct it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;             // your DB id
    private Integer tmdbId;      // TMDB id — frontend uses this for navigation
    private String title;
    private String overview;
    private String posterUrl;    // full URL ready to use in <img src="">
    private String backdropUrl;  // full URL ready to use in hero section
    private String releaseDate;
    private String originalLanguage;
    private Double tmdbRating;
    private String runtime;      // formatted e.g. "2h 28m"
    private String trailerUrl;

    // genres only available on detail view (from TMDB response)
    private List<String> genres;

    // your app's own stats
    private int reviewCount;
    private Double averageRating;
}
